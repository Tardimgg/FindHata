export interface GetAllMessageResponse {
  status: string;
  error: string;
  messages: Message[]
}

export interface Message {
  id: number;
  fromId: number;
  toId: number;
  time: number;
  message: string;
  read: boolean;
}

