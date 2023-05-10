import {Component, Input} from '@angular/core';
import {DialogService} from "../../services/dialog.service";
import {Subscription} from "rxjs";
import {AuthService} from "../../services/auth.service";
import {ChatService} from "../../services/chat.service";


@Component({
  selector: 'app-chat',
  templateUrl: './chat.component.html',
  styleUrls: ['./chat.component.css']
})
export class ChatComponent {

  @Input() proposalId: number;
  @Input() otherId: number;
  @Input() isSeller: boolean;
  @Input() smallMode = false;
  @Input() customTitle = "";
  constructor(private dialogService: DialogService, private authService: AuthService) {
    this.chatService = new ChatService();
  }



  chatService: ChatService;

  messages: string[] = [];
  subscription: Subscription;

  ngOnInit() {
    if (this.smallMode) {
      if (this.isSeller) {
        this.customTitle += " (Вы продавец)";
      } else {
        this.customTitle += " (Вы покупатель)";
      }
    }

    this.messages = []
    this.curMessage = ""

    // @ts-ignore
    this.dialogService.getAll(this.otherId, this.proposalId)
      .subscribe(response => {
        if (response.status == "ok") {
          this.messages = response.messages.sort((a, b) => {
            if (a.time == b.time) {
              return 0;
            } else if (a.time > b.time) {
              return -1;
            } else {
              return 1;
            }

          }).map((v) => {
            if (v.toId == this.otherId) {
              return "Вы: " + v.message;
            } else {
              let other = this.isSeller ? "Покупатель" : "Продавец";
              return other + ": " + v.message;
            }
          });
        }
      })

    this.subscription = this.chatService.messageEmitter.subscribe((mes) => {
      if (mes.toId == this.otherId) {
        this.messages.unshift("Вы: " + mes.message);
      } else {
        let other = this.smallMode ? "Покупатель" : "Продавец";
        this.messages.unshift(other + ": " + mes.message);
      }
    })
    let token = this.authService.getToken();
    if (token != null) {
      this.chatService.connect(this.proposalId, this.authService.getUserId(), this.otherId, token)
    }
  }

  curMessage: string;

  pushMessage() {
    if (this.authService.getUserId() == this.otherId) {
      window.alert("Нельзя отправлять сообщения самому себе")
      return
    }
    let login = this.authService.getLogin();
    if (login != null && login != "") {
      this.chatService.pushMessage(this.curMessage);

      this.messages.unshift("Вы: " + this.curMessage);

      this.curMessage = "";
    } else {
      window.alert("Для отправки сообщений авторизуйтесь")
    }
  }

  ngOnDestroy() {
    if (this.chatService != null) {
      this.chatService.disconnect();
    }
    if (this.subscription != null) {
      this.subscription.unsubscribe();
    }
  }

}
