
import {RxStomp, StompHeaders} from "@stomp/rx-stomp";
import { WebSocket } from 'ws';
import {Subscription} from "rxjs";
import {ChatMessage} from "../entities/requests/chat-message";
import {EventEmitter} from "@angular/core";

// @Injectable({
//   providedIn: 'root'
// })
export class ChatService {

  constructor() {
  }

  // constructor(appComponent: AppComponent){
  //   this.appComponent = appComponent;
  // }

  messageEmitter = new EventEmitter<ChatMessage>();
  rxStomp: RxStomp;
  subscription: Subscription;
  fromId: number;
  toId: number;
  token: string;
  proposalId: number;


  connect(proposalId: number, fromId: number, toId: number, token: string) {
    this.fromId = fromId;
    this.toId = toId;
    this.token = token;
    this.proposalId = proposalId;

    let headers: StompHeaders = {
      login: token
    }

    this.rxStomp = new RxStomp();
    let brokerURL = 'wss://' + document.location.host + "/api-chat/messenger/ws"
    this.rxStomp.configure({
      // brokerURL: 'ws://localhost:8080/messenger/ws',
      brokerURL: brokerURL,
      // connectHeaders: {
      //   login: 'guest',
      //   passcode: 'guest',
      // },
      // brokerURL: 'ws://localhost:8082/ws',
    });

    this.rxStomp.activate() ;

    this.subscription = this.rxStomp
      .watch({ destination: "/room/" + proposalId + "_" + toId + "_" + fromId + "/queue/messages", subHeaders: headers})
      // .watch({ destination: "/user/1/queue/messages" })
      .subscribe((message) => {
        this.messageEmitter.emit(JSON.parse(message.body));
      });
  }

  disconnect() {
    if (this.subscription != null) {
      this.subscription.unsubscribe();
    }
  }


  pushMessage(message: string) {
    let mes: ChatMessage = {
      toId: this.toId,
      message: message,
      proposalId: this.proposalId
    }

    let headers: StompHeaders = {
      login: this.token
    }

    this.rxStomp.publish({
      destination: "/app/chat",
      body: JSON.stringify(mes),
      headers: headers
    });
  }
}
