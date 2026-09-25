import { useState } from 'react'
import { api } from '../api/client'
import { ChoixEtudiant } from '../composants/Choix'
import { Chargement, MessageErreur, MessageSucces } from '../composants/Etat'
import { useAction, useRequete } from '../hooks/useRequete'

/** Écran étudiant (F2) : marquer sa présence, déposer son exercice. */
export default function EcranEtudiant() {
  const [promotionId, setPromotionId] = useState(null)
  const [etudiant, setEtudiant] = useState(null)

  return (
    <>
      <h2>Espace étudiant</h2>
      <ChoixEtudiant promotionId={promotionId} onPromotion={setPromotionId} etudiant={etudiant} onEtudiant={setEtudiant} />
      {etudiant && <MarquerPresence key={`p-${etudiant.id}`} etudiant={etudiant} />}
      {etudiant && <DeposerExercice key={`d-${etudiant.id}`} etudiant={etudiant} promotionId={promotionId} />}
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

function DeposerExercice({ etudiant, promotionId }) {
  const [sessionId, setSessionId] = useState('')
  const [lien, setLien] = useState('')
  const sessions = useRequete(`sessions-${promotionId}`, () => api.sessions(promotionId))
  const depot = useAction(api.deposerExercice)
  const ouvertes = sessions.donnees?.filter((s) => !s.cloturee) ?? []

  async function envoyer(evenement) {
    evenement.preventDefault()
    const exercice = await depot.executer(Number(sessionId), etudiant.id, lien)
    if (exercice) setLien('')
  }

  return (
    <section>
      <h3>Déposer mon exercice</h3>
      {sessions.chargement && <Chargement texte="Chargement des sessions…" />}
      <MessageErreur erreur={sessions.erreur} />
      {sessions.donnees && ouvertes.length === 0 && <p className="vide">Aucune session ouverte aux dépôts.</p>}
      {ouvertes.length > 0 && (
        <form onSubmit={envoyer}>
          <label htmlFor="session">Session</label>
          <select id="session" value={sessionId} onChange={(e) => setSessionId(e.target.value)}>
            <option value="">— choisir —</option>
            {ouvertes.map((s) => (
              <option key={s.id} value={s.id}>{s.titre}</option>
            ))}
          </select>
          <label htmlFor="lien">Lien de l'exercice</label>
          <input id="lien" type="text" inputMode="url" value={lien} onChange={(e) => setLien(e.target.value)} placeholder="https://github.com/…" />
          <button className="principal" disabled={depot.enCours || !sessionId || !lien}>
            {depot.enCours ? 'Envoi…' : 'Déposer'}
          </button>
        </form>
      )}
      <MessageErreur erreur={depot.erreur} />
      <MessageSucces>{depot.resultat && `Exercice déposé — statut : ${depot.resultat.statut} (deux pairs vont le relire).`}</MessageSucces>
    </section>
  )
}
