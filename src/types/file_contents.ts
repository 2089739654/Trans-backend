export type FileItem = {
  id: string;
  name: string;
  isFolder?: boolean;
  isOpen?: boolean;
  children?: FileItem[];
};
