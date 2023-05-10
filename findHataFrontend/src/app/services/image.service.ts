import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import {GetAllProposalsResponse} from "../entities/responses/get_all_proposals_response";
import {GetProposalResponse} from "../entities/responses/get_proposal_response";
import {AddProposalResponse} from "../entities/responses/add_proposal_response";
import {AddProposalRequest} from "../entities/requests/add-proposal-request";

@Injectable({
  providedIn: 'root'
})

export class ImageService {

  // private url = 'http://127.0.0.1:8080/proposals/api/proposal/get-all';
  private url = 'http://127.0.0.1:8081/';

  constructor(private httpClient: HttpClient) { }

  saveImage(form: FormData){
    return this.httpClient.post<AddProposalResponse>(this.url + "save-image", form);
  }

}

