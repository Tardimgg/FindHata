import {Component, Input} from '@angular/core'
import {ProposalService} from "../../services/proposal.service";
import {ShortInfoProposal} from "../../entities/responses/get_all_proposals_response";
import {SwiperOptions} from "swiper";
import {Observable, Subscription} from "rxjs";

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html'
})

export class HomeComponent {

  defaultInit() {
    this.getInfoObserver = this.service.getAllProposals()
      .subscribe(response => {
        if (response.status == "ok") {
          this.displayedSearchTitle = "Рекомендации для вас"
          this.proposals = response.proposals;
        }
      })
  }

  constructor(private service: ProposalService) {
    this.defaultInit();
  }

  proposals: ShortInfoProposal[];
  searchData: string;
  displayedSearchTitle: string = "Рекомендации для вас";
  getInfoObserver: Subscription;

  updateProposals() {
    if (this.getInfoObserver != null) {
      this.getInfoObserver.unsubscribe();
    }
    if (this.searchData == null || this.searchData == "") {
      this.defaultInit();
    } else {
      this.getInfoObserver = this.service.getAllProposalsWithKeyword(this.searchData)
        .subscribe(response => {
          if (response.status == "ok") {
            this.displayedSearchTitle = "Результаты поиска"
            this.proposals = response.proposals;
          }
        })
    }
  }

}
