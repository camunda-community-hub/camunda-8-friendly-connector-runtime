

export interface IUser {
  username: string;
  password: Password|null;
  firstname: string;
  lastname: string;
  token?: string;
  profile: string;
  groups: string[];
}

export interface Password {
  value: string;
  encrypted: boolean;
}

export interface Organization {
  oldname: string;
  name: string;
  modified: string;
  active: boolean;
  users: IUser[];
  groups: string[];
  userMemberships: UserMemberships[];
}

export interface UserMemberships {
  username: string;
  profile: string;
  groups: string[];
}
