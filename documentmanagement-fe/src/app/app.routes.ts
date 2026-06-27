import { Routes } from '@angular/router';
import { CreatedDocument } from './pages/created-document/created-document';
import { Home } from './pages/home/home';
import { IncomingDocument } from './pages/incoming-document/incoming-document';
import { IncomingDocumentDetail } from './pages/incoming-document-detail/incoming-document-detail';
import { InterconnectedSystem } from './pages/interconnected-system/interconnected-system';
import { InterconnectedSystemCreate } from './pages/interconnected-system-create/interconnected-system-create';
import { InterconnectedSystemDetail } from './pages/interconnected-system-detail/interconnected-system-detail';
import { LoginComponent } from './pages/login/login';
import { Organization } from './pages/organization/organization';
import { OrganizationRegistration } from './pages/organization-registration/organization-registration';
import { OrganizationDetail } from './pages/organization-detail/organization-detail';
import { OutgoingDocument } from './pages/outgoing-document/outgoing-document';
import { OutgoingDocumentDetail } from './pages/outgoing-document-detail/outgoing-document-detail';
import { User } from './pages/user/user';
import { UserDetail } from './pages/user-detail/user-detail';

export const routes: Routes = [
  {
    path: 'login',
    component: LoginComponent,
  },
  {
    path: 'home',
    component: Home,
  },
  {
    path: 'home/users',
    component: User,
  },
  {
    path: 'home/users/:id',
    component: UserDetail,
  },
  {
    path: 'home/interconnected-systems',
    component: InterconnectedSystem,
  },
  {
    path: 'home/interconnected-systems/new',
    component: InterconnectedSystemCreate,
  },
  {
    path: 'home/interconnected-systems/:systemCode',
    component: InterconnectedSystemDetail,
  },
  {
    path: 'home/organizations',
    component: Organization,
  },
  {
    path: 'home/organizations/:orgCode',
    component: OrganizationDetail,
  },
  {
    path: 'home/organization-registration',
    component: OrganizationRegistration,
  },
  {
    path: 'home/documents/outgoing',
    component: OutgoingDocument,
  },
  {
    path: 'home/documents/outgoing/:documentId',
    component: OutgoingDocumentDetail,
  },
  {
    path: 'home/documents/incoming',
    component: IncomingDocument,
  },
  {
    path: 'home/documents/incoming/:documentId',
    component: IncomingDocumentDetail,
  },
  {
    path: 'home/documents/created',
    component: CreatedDocument,
  },
  {
    path: '',
    redirectTo: 'login',
    pathMatch: 'full',
  },
  {
    path: '**',
    redirectTo: 'login',
  },
];
