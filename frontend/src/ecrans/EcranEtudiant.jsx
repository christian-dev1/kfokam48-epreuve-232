import { useState } from 'react'
import { api } from '../api/client'
import { ChoixEtudiant } from '../composants/Choix'
import { MessageErreur, MessageSucces } from '../composants/Etat'
import { useAction } from '../hooks/useRequete'

/** Écran étudiant (F2) : marquer sa présence, déposer son exercice. */
export default function EcranEtudiant() {
  const [promotionId, setPromotionId] = useState(null)
  const [etudiant, setEtudiant] = useState(null)

  return (
    <>
      <h2>Espace étudiant</h2>
      <ChoixEtudiant promotionId={promotionId} onPromotion={setPromotionId} etudiant={etudiant} onEtudiant={setEtudiant} />
      {etudiant && <MarquerPresence key={etudiant.id} etudiant={etudiant} />}
    </>
  )
}

function MarquerPresence({ etudiant }) {
  const [code, setCode] = useState('')
  const marquage = useAction(api.marquerPresence)

  async function envoyer(evenement) {
    evenement.preventDefault()
    const presence = await marquage.executer(code, etudiant.id)
    if (presence) setCode('')
  }

  return (
    <section>
      <h3>Marquer ma présence</h3>
      <form onSubmit={envoyer}>
        <label htmlFor="code">Code affiché par le formateur</label>
        <input
          id="code"
          value={code}
          onChange={(e) => setCode(e.target.value.toUpperCase())}
          autoComplete="off"
          autoCapitalize="characters"
          maxLength={6}
          placeholder="K7P2QX"
        />
        <button className="principal" disabled={marquage.enCours || !code}>
          {marquage.enCours ? 'Envoi…' : 'Je suis présent'}
        </button>
      </form>
      <MessageErreur erreur={marquage.erreur} />
      <MessageSucces>{marquage.resultat && `Présence enregistrée, ${etudiant.nom}.`}</MessageSucces>
    </section>
  )
}
