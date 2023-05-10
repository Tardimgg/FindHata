import {Component, Input} from '@angular/core';
import {ProposalDetail} from "../../entities/responses/get_proposal_response";
import {SwiperOptions} from "swiper";
import {ProposalService} from "../../services/proposal.service";
import {ActivatedRoute} from "@angular/router";
import {AuthService} from "../../services/auth.service";
import {Role} from "../../entities/role";
import {Location} from "@angular/common";

@Component({
  selector: 'app-proposal-detail',
  templateUrl: './proposal-detail.component.html',
  styleUrls: ['./proposal-detail.component.css']
})
export class ProposalDetailComponent {

  ngOnInit() {
    // @ts-ignore
    const id = +this.route.snapshot.paramMap.get('id');
    this.proposalService.getProposal(id)
      .subscribe(response => {
        if (response.status == "ok") {
          this.proposal = response.proposal;

          this.removeAccess ||= this.proposal.ownerId == this.authService.getUserId();
        }
      })
  }

  constructor(private proposalService: ProposalService, private route: ActivatedRoute,
              public authService: AuthService, private location: Location) {

  }

  removeAccess = this.authService.getRole() == Role.ADMIN;

  removeProposal() {
    this.proposalService.removeProposal(this.proposal.proposalId)
      .subscribe(response => {
        if (response.status == "ok") {
          this.location.back();
        }
    })
  }

  proposal: ProposalDetail;

  role = Role;

  config: SwiperOptions = {
    slidesPerView: 1,
    spaceBetween: 10,
    navigation: true,
    observer: true,
    centeredSlides: true,
    observeParents: true,
    pagination: { clickable: true },
    scrollbar: { draggable: true },
  };

}
