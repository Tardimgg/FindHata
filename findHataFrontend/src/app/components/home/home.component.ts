import {Component, Input} from '@angular/core'
import {ProposalService} from "../../services/proposal.service";
import {ShortInfoProposal} from "../../entities/responses/get_all_proposals_response";
import {SwiperOptions} from "swiper";

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html'
})

export class HomeComponent {

  defaultInit() {
    this.service.getAllProposals()
      .subscribe(response => {
        if (response.status == "ok") {
          this.proposals = response.proposals;
        }
      })
  }

  constructor(private service: ProposalService) {
    this.defaultInit();
  }

  proposals: ShortInfoProposal[];
  searchData: string;

  updateProposals() {
    if (this.searchData == null || this.searchData == "") {
      this.defaultInit();
    } else {
      this.service.getAllProposalsWithKeyword(this.searchData)
        .subscribe(response => {
          if (response.status == "ok") {
            this.proposals = response.proposals;
          }
        })
    }
  }

}
