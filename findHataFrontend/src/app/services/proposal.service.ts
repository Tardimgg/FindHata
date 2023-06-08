import { Injectable } from '@angular/core';
import {HttpClient, HttpHeaders, HttpParams} from '@angular/common/http';
import {GetAllProposalsResponse} from "../entities/responses/get_all_proposals_response";
import {GetProposalResponse} from "../entities/responses/get_proposal_response";
import {AddProposalResponse} from "../entities/responses/add_proposal_response";
import {AddProposalRequest} from "../entities/requests/add-proposal-request";
import {DeleteProposalResponse} from "../entities/responses/delete_proposal_response";
import {map} from "rxjs";


@Injectable({
  providedIn: 'root'
})

export class ProposalService {

  // private url = 'http://127.0.0.1:8080/proposals/api/proposal/get-all';
  // private url = 'http://127.0.0.1:8080/proposals/api/proposal';
  private url = '/api/proposals/api/proposal';

  constructor(private httpClient: HttpClient) { }

  convertAllImages(v: GetAllProposalsResponse) {
    for (let i = 0; i < v.proposals.length; i++) {
      for (let j = 0; j < v.proposals[i].images.length; j++) {
        v.proposals[i].images[j] = "/api" + v.proposals[i].images[j];
      }
    }
    return v;
  }

  convertImages(v: GetProposalResponse) {
    for (let j = 0; j < v.proposal.images.length; j++) {
      v.proposal.images[j] = "/api" + v.proposal.images[j];
    }
    return v;
  }


  getAllProposals(){
    return this.httpClient.get<GetAllProposalsResponse>(this.url + "/get-all").pipe(map(this.convertAllImages));
  }

  getAllProposalsWithKeyword(keyword: string){
    let params = new HttpParams()
      .set("keyword", keyword)

    return this.httpClient.get<GetAllProposalsResponse>(this.url + "/get-all", { params: params }).pipe(map(this.convertAllImages));
  }

  getProposal(id: number){
    return this.httpClient.get<GetProposalResponse>(this.url + "/get/" +  id).pipe(map(this.convertImages));
  }

  removeProposal(id: number){
    return this.httpClient.delete<DeleteProposalResponse>(this.url + "/get/" +  id);
  }

  addProposal(proposal: AddProposalRequest){
    return this.httpClient.post<AddProposalResponse>(this.url, proposal);
  }
}
