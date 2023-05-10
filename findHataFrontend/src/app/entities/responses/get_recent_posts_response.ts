import {Message} from "./get_all_message_response";

export interface GetRecentPostsResponse {
  status: string;
  error: string;
  recentPosts: RecentPost[]
}

export interface RecentPost {
  message: Message,
  proposalId: number
}
