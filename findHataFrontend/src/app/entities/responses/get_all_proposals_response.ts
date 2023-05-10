export interface GetAllProposalsResponse {
  status: string,
  proposals: ShortInfoProposal[];
}

export interface ShortInfoProposal {
  id: number;
  title: string;
  price: number;
  location: string;
  images: string[];
}
