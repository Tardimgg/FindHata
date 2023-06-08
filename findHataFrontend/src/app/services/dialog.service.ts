import {EventEmitter, Injectable} from '@angular/core';
import {HttpClient, HttpHeaders, HttpParams} from '@angular/common/http';
import {GetAllProposalsResponse} from "../entities/responses/get_all_proposals_response";
import {GetProposalResponse} from "../entities/responses/get_proposal_response";
import {AddProposalResponse} from "../entities/responses/add_proposal_response";
import {AddProposalRequest} from "../entities/requests/add-proposal-request";
import {GetAllMessageResponse} from "../entities/responses/get_all_message_response";
import {ChatMessage} from "../entities/requests/chat-message";
import {GetRecentPostsResponse} from "../entities/responses/get_recent_posts_response";


@Injectable({
  providedIn: 'root'
})
export class DialogService {

  // private url = 'http://127.0.0.1:8080/messenger/api/message';
  private url = '/api/messenger/api/message';

  constructor(private httpClient: HttpClient) { }

  getAll(withId: number, proposalId: number){
    let params = new HttpParams()
      .set("withId", withId)
      .set("proposalId", proposalId);

    // return this.httpClient.get<GetAllMessageResponse>(this.url + "/get-all" + "?withId=" + withId);
    return this.httpClient.get<GetAllMessageResponse>(this.url + "/get-all", {params: params});
  }

  getRecentPosts(){
    return this.httpClient.get<GetRecentPostsResponse>(this.url + "/get-all-small");
  }

}
