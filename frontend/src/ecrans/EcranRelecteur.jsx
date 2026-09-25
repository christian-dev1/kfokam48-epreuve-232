import { useState } from 'react'
import { api } from '../api/client'
import { ChoixEtudiant } from '../composants/Choix'
import { Chargement, MessageErreur, MessageSucces } from '../composants/Etat'
import { useAction, useRequete } from '../hooks/useRequete'

/** Écran relecteur (F2) : l'étudiant voit les exercices qu'il doit relire et rend sa note. */
export default function EcranRelecteur() {
  const [promotionId, setPromotionId] = useState(null)
  const [etudiant, setEtudiant] = useState(null)

  return (
    <>
      <h2>Espace relecteur</h2>
      <ChoixEtudiant promotionId={promotionId} onPromotion={setPromotionId} etudiant={etudiant} onEtudiant={setEtudiant} />
      {etudiant && <MesRelectures key={etudiant.id} relecteur={etudiant} />}
    </>
  )
}

function MesRelectures({ relecteur }) {
  const liste = useRequete(`relectures-${relecteur.id}`, () => api.relectures(relecteur.id))

  return (
    <section>
      <h3>Mes relectures</h3>
      {liste.chargement && <Chargement />}
      <MessageErreur erreur={liste.erreur} />
      {liste.donnees?.length === 0 && <p className="vide">Aucune relecture ne t'est assignée.</p>}
      {liste.donnees?.map((r) => (
        <Relecture key={r.id} relecture={r} relecteurId={relecteur.id} onRendue={liste.recharger} />
      ))}
    </section>
  )
}

function Relecture({ relecture, relecteurId, onRendue }) {
  const [note, setNote] = useState('')
  const [commentaire, setCommentaire] = useState('')
  const envoi = useAction(api.rendreRelecture)

  async function envoyer(evenement) {
    evenement.preventDefault()
    // La note est envoyée telle quelle : c'est l'API qui applique RG13 (entier de 0 à 20)
    const resultat = await envoi.executer(relecture.id, note === '' ? null : Number(note), commentaire, relecteurId)
    if (resultat) onRendue()
  }

  return (
    <article style={{ borderTop: '1px solid #e4e7ec', paddingTop: 8, marginTop: 8 }}>
      <p>
        <strong>{relecture.sessionTitre}</strong> —{' '}
        <a href={relecture.lien} target="_blank" rel="noreferrer">ouvrir l'exercice</a>{' '}
        {relecture.rendue ? <span className="badge">rendue</span> : <span className="badge attente">à faire</span>}
      </p>
      {relecture.rendue ? (
        <p>Note envoyée : <strong>{relecture.note}/20</strong> — « {relecture.commentaire} » (définitive ; la note retenue sera la moyenne avec l'autre relecteur)</p>
      ) : (
        <form onSubmit={envoyer}>
          <label htmlFor={`note-${relecture.id}`}>Note sur 20</label>
          <input id={`note-${relecture.id}`} type="text" inputMode="numeric" value={note} onChange={(e) => setNote(e.target.value)} />
          <label htmlFor={`com-${relecture.id}`}>Commentaire</label>
          <textarea id={`com-${relecture.id}`} rows={3} value={commentaire} onChange={(e) => setCommentaire(e.target.value)} />
          <button className="principal" disabled={envoi.enCours}>{envoi.enCours ? 'Envoi…' : 'Envoyer (définitif)'}</button>
        </form>
      )}
      <MessageErreur erreur={envoi.erreur} />
      <MessageSucces>{envoi.resultat && 'Relecture enregistrée.'}</MessageSucces>
    </article>
  )
}
