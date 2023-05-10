import { Component } from '@angular/core';
import {AuthService} from "../../services/auth.service";
import {ProposalService} from "../../services/proposal.service";
import {ShortInfoProposal} from "../../entities/responses/get_all_proposals_response";
import {DialogService} from "../../services/dialog.service";
import {Message} from "../../entities/responses/get_all_message_response";
import {RecentPost} from "../../entities/responses/get_recent_posts_response";

@Component({
  selector: 'app-dialogs',
  templateUrl: './dialogs.component.html',
  styleUrls: ['./dialogs.component.css']
})
export class DialogsComponent {

  constructor(private dialogService: DialogService, private authService: AuthService,
              private proposalService: ProposalService) {

    this.dialogService.getRecentPosts()
      .subscribe(dialogResponse => {
        if (dialogResponse.status == "ok") {
          this.recentPosts = dialogResponse.recentPosts;

          let curId = authService.getUserId();

          for (let i = 0; i < this.recentPosts.length; ++i) {
            proposalService.getProposal(this.recentPosts[i].proposalId).subscribe(proposalResponse => {

              if (proposalResponse.status == "ok") {
                if (curId == this.recentPosts[i].message.fromId) {
                  this.owners.push([this.recentPosts[i].message.toId, proposalResponse.proposal.title,
                    proposalResponse.proposal.proposalId, proposalResponse.proposal.ownerId == curId])
                } else {
                  this.owners.push([this.recentPosts[i].message.fromId, proposalResponse.proposal.title,
                    proposalResponse.proposal.proposalId, proposalResponse.proposal.ownerId == curId])
                }
              }
            })
          }
        }
      })
  }

  recentPosts: RecentPost[];
  owners: [number, string, number, boolean][] = [];

}
