import { api } from '../api/client'
import { useRequete } from '../hooks/useRequete'
import { Chargement, MessageErreur } from './Etat'

/** Liste déroulante des promotions. */
export function ChoixPromotion({ valeur, onChange }) {
  const { donnees, chargement, erreur } = useRequete('promotions', api.promotions)
  if (chargement) return <Chargement texte="Chargement des promotions…" />
  if (erreur) return <MessageErreur erreur={erreur} />
  return (
    <>
      <label htmlFor="promotion">Promotion</label>
      <select id="promotion" value={valeur ?? ''} onChange={(e) => onChange(e.target.value ? Number(e.target.value) : null)}>
        <option value="">— choisir —</option>
        {donnees.map((p) => (
          <option key={p.id} value={p.id}>{p.nom}</option>
        ))}
      </select>
    </>
  )
}

/** « L'étudiant choisit son nom dans une liste » (Q1, EF2) : promotion puis nom, sans mot de passe. */
export function ChoixEtudiant({ promotionId, onPromotion, etudiant, onEtudiant }) {
  const { donnees, chargement, erreur } = useRequete(
    promotionId ? `etudiants-${promotionId}` : null,
    () => api.etudiants(promotionId),
  )
  return (
    <section>
      <ChoixPromotion
        valeur={promotionId}
        onChange={(id) => {
          onPromotion(id)
          onEtudiant(null)
        }}
      />
      {chargement && <Chargement texte="Chargement des étudiants…" />}
      <MessageErreur erreur={erreur} />
      {donnees && (
        <>
          <label htmlFor="etudiant">Je suis…</label>
          <select
            id="etudiant"
            value={etudiant?.id ?? ''}
            onChange={(e) => onEtudiant(donnees.find((x) => x.id === Number(e.target.value)) ?? null)}
          >
            <option value="">— choisir mon nom —</option>
            {donnees.map((x) => (
              <option key={x.id} value={x.id}>{x.nom}</option>
            ))}
          </select>
        </>
      )}
    </section>
  )
}
