// types.ts
interface UserGroup {
  id: string;
  name: string;
  description?: string;
  projects: Project[];
}
interface FileNode {
  id: string;
  name: string;
  isFolder: boolean;
  isOpen?: boolean;
  children?: FileNode[];
}
interface Project {
  id: string;
  name: string;
  isFolder: true; // 项目本质是文件夹
  isOpen: boolean;
  children: FileNode[];
}

