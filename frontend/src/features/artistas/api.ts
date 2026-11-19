import { http } from '../../lib/http'
import * as t from '../tipos'
const e = encodeURIComponent
export const artistasApi = {
  artistas: () => http('/artists/active', t.Artista.array()),
  artista: (id: number) => http(`/artists/${id}`, t.Artista),
  artistaNombre: (name: string) => http(`/artists/by-stage-name?stageName=${e(name)}`, t.Artista),
}
