export interface GetProposalResponse {
  status: string,
  proposal: ProposalDetail;
}

export interface ProposalDetail {
  proposalId: number;
  title: string;
  price: number;
  description: string;
  ownerId: number;
  location: string;
  images: string[];
}
