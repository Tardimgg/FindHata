import {Component, Input, ViewEncapsulation} from '@angular/core'
import {ShortInfoProposal} from "../../entities/responses/get_all_proposals_response";
// import Swiper core and required modules
import SwiperCore, {A11y, EffectCoverflow, Navigation, Pagination, Scrollbar, SwiperOptions} from "swiper";

// import 'swiper/css';
// import 'swiper/css/navigation';
// import 'swiper/css/pagination';


// install Swiper modules
SwiperCore.use([Navigation, Pagination, Scrollbar, A11y])
// SwiperCore.use([EffectCoverflow]);
// SwiperCore.use([Navigation]);

@Component({
  selector: 'app-proposal',
  templateUrl: './short-info-proposal.component.html',
  encapsulation: ViewEncapsulation.None
})
export class ShortInfoProposalComponent {

  @Input() proposal: ShortInfoProposal;

  config: SwiperOptions = {
    slidesPerView: 1,
    spaceBetween: 10,
    navigation: true,
    observer: true,
    observeParents: true,
    pagination: { clickable: true },
    scrollbar: { draggable: true },
  };


  // config: SwiperOptions = {
  //   pagination: {
  //     el: '.swiper-pagination',
  //     clickable: true
  //   },
  //   navigation: {
  //     nextEl: '.swiper-button-next',
  //     prevEl: '.swiper-button-prev'
  //   },
  //   spaceBetween: 30
  // };


}
