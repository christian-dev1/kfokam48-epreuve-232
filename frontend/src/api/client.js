// F3 — Couche d'accès à l'API : c'est le SEUL fichier du frontend qui appelle fetch.
// Toutes les erreurs sont converties en ErreurApi { statut, code, message } (format du contrat).

const BASE = import.meta.env.VITE_API_URL ?? 'http://localhost:8080'

export class ErreurApi extends Error {
  constructor(statut, code, message) {
    super(message)
    this.statut = statut
    this.code = code
  }
}

export async function requete(chemin, { methode = 'GET', corps } = {}) {
  let reponse
  try {
    reponse = await fetch(BASE + chemin, {
      method: methode,
      headers: corps !== undefined ? { 'Content-Type': 'application/json' } : undefined,
      body: corps !== undefined ? JSON.stringify(corps) : undefined,
    })
  } catch {
    throw new ErreurApi(0, 'SERVEUR_INJOIGNABLE', "Le serveur est injoignable : vérifiez que l'API est démarrée.")
  }

  const texte = await reponse.text()
  let donnees = null
  if (texte) {
    try {
      donnees = JSON.parse(texte)
    } catch {
      donnees = null
    }
  }

  if (!reponse.ok) {
    throw new ErreurApi(reponse.status, donnees?.code ?? 'ERREUR', donnees?.message ?? `Erreur ${reponse.status}`)
  }
  return donnees
}

export const api = {}
