import {Role} from "../role";

export interface LoginResponse {
  status: string,
  accessToken: string;
  roles: Role[]
  error: string;
  userId: number
}
