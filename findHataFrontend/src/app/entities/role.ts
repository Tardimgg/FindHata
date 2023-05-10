export enum Role {
  ADMIN, USER, ANONYMOUS

}

export function roleFrom(str: string) {
  return Role[str as keyof typeof Role];
}
