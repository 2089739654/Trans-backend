package com.example.trans_backend_gateway.config;


import lombok.Data;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;
@Configuration
@ConfigurationProperties(prefix = "gateway")
@Data
public class IpRateConfig {


    private  List<IpRate> ipRateList;


    public static class IpRate {

        private String ip;

        private int rate;
        private int capacity;

        public String getIp() {
            return ip;
        }

        public void setIp(String ip) {
            this.ip = ip;
        }

        public int getRate() {
            return rate;
        }

        public void setRate(int rate) {
            this.rate = rate;
        }

        public int getCapacity() {
            return capacity;
        }

        public void setCapacity(int capacity) {
            this.capacity = capacity;
        }
    }

    public int[] getByIp(String ip){
        for (IpRate ipRate:ipRateList){
            if(ipRate.getIp().equals(ip)){
                return new int[]{ipRate.getRate(),ipRate.getCapacity()};
            }
        }
        return null;
    }


}
