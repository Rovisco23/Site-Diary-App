import {FormControl, FormGroupDirective, NgForm} from "@angular/forms";
import {ErrorStateMatcher} from "@angular/material/core";

export interface Classes {
  id: string;
  name: string;
  owner: string;
  description: string;
  address: Address;
  type: string;
  state: string;
  verification: boolean;
}

export interface Address {
  location: Location,
  street: String,
  postalCode: String
}

interface Location {
  district: string,
  county: string,
  parish: string
}

export interface Pending {
  id: number,
  name: string,
  email: string,
  username: string,
  nif: number,
  location: Location,
  association: Association
}

export interface Member {
  id: number;
  name: string;
  role: string;
}

export interface Work {
  id: string;
  name: string;
  description: string;
  address: Address;
  type: string;
  licenseHolder: string;
  state: string;
  company: Company;
  building: string;
  members: Member[];
  log: LogEntrySimplified[];
  technicians: Technician[];
  images: number;
  docs: number;
  verification: boolean;
  verificationDoc: string | null;
}

export interface Technician {
  name: string,
  email: string,
  role: string,
  association: Association
}

export interface TechnicianCreation {
  position: number,
  name: string,
  email: string,
  role: string,
  association: Association,
  submitted: boolean
}

export interface LogEntrySimplified {
  id: number,
  author: Author,
  editable: boolean,
  createdAt: string,
  attachments: boolean
}

export interface LogEntry {
  workId: string,
  content: string,
  editable: boolean,
  createdAt: string,
  modifiedAt: string,
  author: Author,
  files: SimpleFile[]
}

export interface MyLog {
  id: number,
  workId: string,
  workName: string,
  author: number,
  editable: boolean,
  attachments: boolean,
  createdAt: string
}

export interface SimpleFile {
  id: number;
  fileName: string;
  contentType: string;
  uploadDate: string
}

export interface InviteSimplified {
  id: string,
  workId: string,
  workTitle: string,
  admin: string,
  role: string
}

export interface MemberProfile {
  id: string,
  name: string,
  role: string,
  email: string,
  phone: string | null,
  location: Location
}

interface Author {
  id: number,
  name: string,
  role: string
}

export interface Company {
  name: string;
  num: number;
}

export interface EditWorkInputModel {
  name: string,
  description: string,
  address: Address,
  type: string,
  licenseHolder: string,
  company: Company,
  building: string,
  technicians: Technician[]
}

export interface User {
  id: string
  username: string,
  email: string,
  phone: string | null,
  nif: number,
  firstName: string,
  lastName: string,
  role: string,
  location: Location,
  association: Association
}

export interface InputWork {
  name: string;
  type: string;
  description: string;
  holder: string;
  company: Company;
  building: string;
  address: Address;
  technicians: Technician[];
  verification: string | null
}

export enum WorkTypes {
  Residential = 'Residencial',
  Comercial = 'Comercial',
  Industrial = 'Industrial',
  Infrastructural = 'Infraestrutura',
  Institutional = 'Institucional',
  Rehabilitation = 'Reabilitação',
  Special_Structure = 'Estrutura Especial',
  Work_of_Art = 'Obra De Arte',
  Habitation = 'Habitação',
  Special_Building = 'Edificio Especial'
}

export interface Invite {
  position: number;
  email: string;
  role: string;
}

export interface InviteCreation {
  position: number;
  email: string;
  role: string;
  submitted: boolean;
}

export interface AnswerInvite {
  id: string;
  workId: string | null;
  accepted: boolean;
  role: string;
}

export interface Association {
  name: string;
  number: number;
}

export interface OpeningTermLocation {
  county: string,
  parish: string,
  street: string,
  postalCode: string,
  building: string
}

export interface OpeningTermVerification {
  doc: string,
  signature: string,
  dt_signature: string
}

export interface OpeningTermAuthor {
  name: string,
  association: string,
  num: number
}

export interface OpeningTerm {
  verification: OpeningTermVerification,
  location: OpeningTermLocation,
  licenseHolder: string,
  authors: Map<string, OpeningTermAuthor>,
  company: Company,
  type: string,
}

export interface Verification {
  id: string,
  owner: string,
  name: string,
  type: string,
  address: Address
}

export interface Password {
  passwordValue: string
}

export class Role {

  private static composedRoles: { [key: string]: string } = {
    'DONO': 'Dono da Obra',
    'MEMBRO': 'Membro',
    'ESPECTADOR': 'Espectador',
    'FISCALIZAÇÃO': 'Responsável de Fiscalização',
    'COORDENADOR': 'Coordenador',
    'ARQUITETURA': 'Técnico de Arquitetura',
    'ESTABILIDADE': 'Técnico de Estabilidade',
    'ELETRICIDADE': 'Técnico de Alimentação e Destribuição de Energia Elétrica',
    'GÁS': 'Técnico de Instalações de Gás',
    'CANALIZAÇÃO': 'Técnico de Instalações de Água e Esgotos',
    'TELECOMUNICAÇÕES': 'Técnico de Instalações de Telecomunicações',
    'TERMICO': 'Técnico de Comportamento Térmico',
    'ACUSTICO': 'Técnico de Condicionamento Acústico',
    'TRANSPORTES': 'Técnico de Instalações de Eletromecânicas de Transporte',
    'DIRETOR': 'Diretor de Obra'
  }

  private static invertedRoles = Role.getInvertedRoles();

  public static getInvertedRoles(): { [key: string]: string } {
    const inverted: { [key: string]: string } = {};
    for (const key in Role.composedRoles) {
      if (Role.composedRoles.hasOwnProperty(key)) {
        inverted[Role.composedRoles[key]] = key;
      }
    }
    return inverted;
  }

  public static composeRole(role: string): string {
    return Role.composedRoles[role] || role;
  }

  public static decomposeRole(roleDescription: string): string {
    return Role.invertedRoles[roleDescription] || roleDescription;
  }
}

export class WorkState {

  public static composeState(state: string){
    switch (state) {
      case 'IN_PROGRESS':
        return 'Em Progresso';
      case 'FINISHED':
        return 'Terminada';
      case 'REJECTED':
        return 'Rejeitada';
      case 'VERIFYING':
        return 'Em Verificação';
      default:
        return state;
    }
  }

  public static decomposeState(state: string): string {
    switch (state) {
      case 'Em Progresso':
        return 'IN_PROGRESS';
      case 'Terminada':
        return 'FINISHED';
      case 'Rejeitada':
        return 'REJECTED';
      case 'Em Verificação':
        return 'VERIFYING';
      default:
        return state;
    }
  }

  public static composeType(type: string): string {
    switch (type) {
      case 'RESIDENCIAL':
        return 'Residencial';
      case 'COMERCIAL':
        return 'Comercial';
      case 'INDUSTRIAL':
        return 'Industrial';
      case 'INFRAESTRUTURA':
        return 'Infraestrutura';
      case 'INSTITUCIONAL':
        return 'Institucional';
      case 'REABILITACAO': // Corrigi a ausência do 'Ç' em 'REABILITAÇÃO' para funcionar no código
        return 'Reabilitação';
      case 'ESTRUTURA ESPECIAL':
        return 'Estrutura Especial';
      case 'OBRA DE ARTE':
        return 'Obra de Arte';
      case 'HABITACAO': // Corrigi a ausência do 'Ç' em 'HABITAÇÃO' para funcionar no código
        return 'Habitação';
      case 'EDIFICIO ESPECIAL':
        return 'Edifício Especial';
      default:
        return 'Tipo Desconhecido';
    }
  }

  public static decomposeType(type: string): string {
    switch (type) {
      case 'Residencial':
        return 'RESIDENCIAL';
      case 'Comercial':
        return 'COMERCIAL';
      case 'Industrial':
        return 'INDUSTRIAL';
      case 'Infraestrutura':
        return 'INFRAESTRUTURA';
      case 'Institucional':
        return 'INSTITUCIONAL';
      case 'Reabilitação':
        return 'REABILITACAO'; // Corrigi a ausência do 'Ç' em 'REABILITAÇÃO' para funcionar no código
      case 'Estrutura Especial':
        return 'ESTRUTURA ESPECIAL';
      case 'Obra de Arte':
        return 'OBRA DE ARTE';
      case 'Habitação':
        return 'HABITACAO'; // Corrigi a ausência do 'Ç' em 'HABITAÇÃO' para funcionar no código
      case 'Edifício Especial':
        return 'EDIFICIO ESPECIAL';
      default:
        return 'Tipo Desconhecido';
    }
  }

}
