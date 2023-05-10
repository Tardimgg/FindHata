export interface AddProposalRequest {
  title: string;
  description: string;
  location: string;
  images: string[];
  price: number;

}

export interface Image {
  base64: string;
}
