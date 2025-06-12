package com.example.canal.deploy;

import cn.hutool.core.thread.ThreadFactoryBuilder;
import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.client.CanalConnectors;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.CanalEntry.*;
import com.alibaba.otter.canal.protocol.Message;
import com.example.trans_backend_common.exception.BusinessException;
import com.example.trans_backend_common.exception.ErrorCode;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

@Component
@ConfigurationProperties(prefix = "canal")
@Data
@ConditionalOnProperty(name = "canal.enabled", havingValue = "true")
@Slf4j
public class CanalClient implements InitializingBean {

//todo 优雅启停

    private final static int BATCH_SIZE = 100;

    private String hostname;

    private int port;

    private String destination;

    private String username;

    private String password;

    private final String indexName="trans_pairs";

    @Resource
    private RestHighLevelClient restHighLevelClient;

    private ConcurrentSkipListSet<Long> concurrentSkipListSet=new ConcurrentSkipListSet<>();

//    private ThreadPoolExecutor threadPoolExecutor=new ThreadPoolExecutor(5,10,30, TimeUnit.MINUTES,new ArrayBlockingQueue<>(30));
//
//
//    {
//        threadPoolExecutor.setThreadFactory(ThreadFactoryBuilder.create().setNamePrefix("canal-pool-").setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
//            @Override
//            public void uncaughtException(Thread t, Throwable e) {
//                throw new BusinessException(ErrorCode.SYSTEM_ERROR,"canal线程池异常:"+e.getMessage());
//            }
//        }).build());
//        //todo
//        threadPoolExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
//    }


    //线程池处理   有待商榷 todo
    //异步ack   todo

    @Override
    public void afterPropertiesSet() throws Exception {
        // 创建链接
        CanalConnector connector = CanalConnectors.newSingleConnector(new InetSocketAddress(hostname, port), destination, username, password);
        try {
            //打开连接
            connector.connect();
            //订阅数据库表,全部表
            connector.subscribe("trans_backend\\.translation_pairs");
            //回滚到未进行ack的地方，下次fetch的时候，可以从最后一个没有ack的地方开始拿
            connector.rollback();
            int fail=0;
            while (true) {
                // 获取指定数量的数据
                Message message = connector.getWithoutAck(BATCH_SIZE);
                //获取批量ID
                long batchId = message.getId();
                //获取批量的数量
                int size = message.getEntries().size();
                //如果没有数据
                if (batchId == -1 || size == 0) {
                    try {
                        //线程休眠2秒
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        log.info("canal线程被中断");
                        e.printStackTrace();
                    }
                } else {
                    //如果有数据,处理数据
                    try {
                        resolve(message.getEntries());
                    } catch (Exception e) {
                        log.error("处理数据失败,批量ID:{},数量:{}", batchId, size,e);
                        fail++;
                        if(fail>=3){
                            //如果失败次数大于3次，进行ack
                            log.error("处理数据失败超过3次,批量ID:{},数量:{}", batchId, size);
                            connector.ack(batchId);
                            fail=0;
                            //todo
                        }
                        continue;
                    }
                    connector.ack(batchId);
                }
                //进行 batch id 的确认。确认之后，小于等于此 batchId 的 Message 都会被确认。
                fail=0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            connector.disconnect();
        }
    }

    private void resolve(List<Entry> entries){
        BulkRequest bulkRequest=new BulkRequest();
        for(Entry entry:entries){
            if (entry.getEntryType() == EntryType.TRANSACTIONBEGIN || entry.getEntryType() == EntryType.TRANSACTIONEND) {
                //开启/关闭事务的实体类型，跳过
                continue;
            }
            RowChange rowChange;
            try {
                rowChange = RowChange.parseFrom(entry.getStoreValue());
            } catch (Exception e) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR,"canal解析数据失败:"+entry+e.getMessage());
            }

            EventType eventType = rowChange.getEventType();
            if(rowChange.getIsDdl()){
                continue;
            }
            for (RowData rowData:rowChange.getRowDatasList()) {
                // 根据事件类型处理数据
                switch (eventType) {
                    case INSERT:
                        handleInsert(bulkRequest, indexName, rowData);
                        break;
                    case UPDATE:
                        handleUpdate(bulkRequest, indexName, rowData);
                        break;
                    case DELETE:
                        handleDelete(bulkRequest, indexName, rowData);
                        break;
                    default:
                        log.error("Unsupported event type: {}", eventType);
                }
            }

        }
        if (!bulkRequest.requests().isEmpty()) {
            try {
                BulkResponse response = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
                if(response.hasFailures()){
                    log.error("Bulk request failed: {}", response.buildFailureMessage());
                }
            } catch (IOException e) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR,"es批量请求失败:"+e.getMessage());
            }
        }

    }


    private void handleInsert(BulkRequest bulkRequest, String indexName, CanalEntry.RowData rowData) {
        Map<String, Object> data = convertToMap(rowData.getAfterColumnsList());
        String id = getIdValue(rowData.getAfterColumnsList());

        IndexRequest indexRequest = new IndexRequest(indexName)
                .id(id)
                .source(data, XContentType.JSON);

        bulkRequest.add(indexRequest);
    }

    private void handleUpdate(BulkRequest bulkRequest, String indexName, CanalEntry.RowData rowData) {
        Map<String, Object> data = convertToMap(rowData.getAfterColumnsList());
        String id = getIdValue(rowData.getAfterColumnsList());

        IndexRequest indexRequest = new IndexRequest(indexName)
                .id(id)
                .source(data, XContentType.JSON);

        bulkRequest.add(indexRequest);
    }

    private void handleDelete(BulkRequest bulkRequest, String indexName, CanalEntry.RowData rowData) {
        String id = getIdValue(rowData.getBeforeColumnsList());
         DeleteRequest deleteRequest = new DeleteRequest(indexName, id);
         bulkRequest.add(deleteRequest);
    }


    private String getIdValue(List<CanalEntry.Column> columns) {
        // 查找主键列，通常是名为"id"的列，但需根据实际表结构调整
        for (CanalEntry.Column column : columns) {
            if (column.getIsKey()) {
                return column.getValue();
            }
        }
        // 如果没有主键，使用时间戳+随机数生成一个唯一ID
        return System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8);
    }

    private Map<String, Object> convertToMap(List<CanalEntry.Column> columns) {
        Map<String, Object> map = new HashMap<>();

        for (CanalEntry.Column column : columns) {
            String name = column.getName();
            String value = column.getValue();

            // 处理不同类型的字段
            if (value == null || value.isEmpty()) {
                //不处理空值
                continue;
            }
            if(name.equals("is_new")){
                if(value.equals("1")){
                    map.put(snakeToCamelManual(name), true);
                }else {
                    map.put(snakeToCamelManual(name), false);
                }
                continue;
            }
            // 简单类型转换，实际使用时可能需要更复杂的类型映射
            try {
                map.put(snakeToCamelManual(name), value);
            } catch (NumberFormatException e) {
                // 不是数字类型，作为字符串处理
                map.put(snakeToCamelManual(name), value);
            }
        }

        return map;
    }
    public static String snakeToCamelManual(String snakeCase) {
        if (snakeCase == null || snakeCase.isEmpty()) {
            return snakeCase;
        }
        StringBuilder result = new StringBuilder();
        boolean capitalizeNext = false;

        for (char c : snakeCase.toCharArray()) {
            if (c == '_') {
                capitalizeNext = true;
            } else {
                if (capitalizeNext) {
                    result.append(Character.toUpperCase(c));
                    capitalizeNext = false;
                } else {
                    result.append(c);
                }
            }
        }
        return result.toString();
    }


}
