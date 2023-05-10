import { Component } from '@angular/core';
import {ProposalService} from "../../services/proposal.service";
import {AddProposalRequest} from "../../entities/requests/add-proposal-request";
import {ImageService} from "../../services/image.service";
import {Observable} from "rxjs";

@Component({
  selector: 'app-create-proposal',
  templateUrl: './create-proposal.component.html',
  styleUrls: ['./create-proposal.component.css']
})
export class CreateProposalComponent {

  constructor(private proposalService: ProposalService, private imageService: ImageService) {}


  proposal = {
    title: '',
    description: '',
    location: '',
    price: 0,
  }

  filesToUpload: FileList;

  handleFileInput(event: Event) {
    // @ts-ignore
    this.filesToUpload = event.target.files;
  }

  textError: string;



  createProposal() {

    new Observable<string[]>((obs) => {
      let images: string[] = []

      if (this.filesToUpload != null) {
        for (let i = 0; i < this.filesToUpload.length; i++) {
          let form: FormData = new FormData();
          form.set("image", this.filesToUpload[i])
          this.imageService.saveImage(form).subscribe(response => {
            images.push(response.path)

            if (i + 1 == this.filesToUpload.length) {
              obs.next(images);
              obs.complete();
            }
          })
        }
      } else {
        obs.next(images);
        obs.complete();
      }

    }).subscribe(imagePaths => {

      let request: AddProposalRequest = {
        title: this.proposal.title,
        description: this.proposal.description,
        location: this.proposal.location,
        images: imagePaths,
        price: this.proposal.price,
      }

      this.proposalService.addProposal(request).subscribe(response => {
        if (response.status == "ok") {
          this.textError = "Успешно";

        } else {
          window.alert("Для добавления объявления необходимо зарегистрироваться и подтвердить аккаунт")
          this.textError = response.error;
        }
      })
    })
  }
}
