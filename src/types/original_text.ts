export type OriginalText = {
  title: string;
  fullText: TextEntry[];
};

export type TextEntry = {
  originalText: string;
  baseText: string;
  finalText: string;
};
