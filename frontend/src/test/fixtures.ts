export const principal = {
  userId: 1,
  username: 'andrea',
  email: 'andrea@pulsepass.test',
  roles: ['USER'],
}
export const usuario = {
  id: 1,
  ...principal,
  firstName: 'Andrea',
  lastName: 'Ruiz',
  phone: null,
  city: 'Santa Marta',
  birthDate: '1998-04-12',
  active: true,
}
export const evento = {
  id: 1,
  eventCode: 'CMF-2026',
  name: 'Caribbean Music Fest 2026',
  category: 'MUSIC',
  status: 'PUBLISHED',
  eventDate: '2026-12-12T20:00:00',
  venueName: 'Marina Convention Center',
  venueCode: 'VEN-SMR-01',
  minimumAge: 18,
  description: 'Una noche inolvidable.',
  artists: ['Solar Beat'],
}
export const artista = {
  id: 1,
  stageName: 'Solar Beat',
  country: 'Colombia',
  genre: 'Electrónica',
  active: true,
}
export const sede = {
  id: 1,
  code: 'VEN-SMR-01',
  name: 'Marina Convention Center',
  city: 'Santa Marta',
  address: 'Carrera 1 # 2-3',
  capacity: 3,
  active: true,
}
export const ticket = {
  id: 1,
  ticketCode: 'TKT-8F2A1C',
  type: 'VIP',
  price: 250000,
  status: 'PAID',
  purchaseDate: '2026-11-05T14:32:00',
  userEmail: principal.email,
  eventCode: evento.eventCode,
  eventName: evento.name,
}
