import { http } from '../../lib/http'
import * as t from '../tipos'
const e = encodeURIComponent
export const sedesApi = {
  sedes: () => http('/venues/active', t.Sede.array()),
  sede: (c: string) => http(`/venues/${e(c)}`, t.Sede),
}
