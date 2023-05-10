import { Injectable } from '@angular/core';
import {HttpClient, HttpHeaders, HttpParams} from '@angular/common/http';
import {GetAllProposalsResponse} from "../entities/responses/get_all_proposals_response";
import {GetProposalResponse} from "../entities/responses/get_proposal_response";
import {AddProposalResponse} from "../entities/responses/add_proposal_response";
import {AddProposalRequest} from "../entities/requests/add-proposal-request";
import {DeleteProposalResponse} from "../entities/responses/delete_proposal_response";

@Injectable({
  providedIn: 'root'
})

export class ProposalService {

  // private url = 'http://127.0.0.1:8080/proposals/api/proposal/get-all';
  private url = 'http://127.0.0.1:8080/proposals/api/proposal';

  constructor(private httpClient: HttpClient) { }

  getAllProposals(){
    return this.httpClient.get<GetAllProposalsResponse>(this.url + "/get-all");
  }

  getAllProposalsWithKeyword(keyword: string){
    let params = new HttpParams()
      .set("keyword", keyword)

    return this.httpClient.get<GetAllProposalsResponse>(this.url + "/get-all", { params: params });
  }

  getProposal(id: number){
    return this.httpClient.get<GetProposalResponse>(this.url + "/get/" +  id);
  }

  removeProposal(id: number){
    return this.httpClient.delete<DeleteProposalResponse>(this.url + "/get/" +  id);
  }

  addProposal(proposal: AddProposalRequest){
    return this.httpClient.post<AddProposalResponse>(this.url, proposal);
  }
}
