import { useState } from 'react'
import { api } from '../api/client'
import { ChoixPromotion } from '../composants/Choix'
import { Chargement, MessageErreur } from '../composants/Etat'
import { dateHeure, heure } from '../composants/format'
import { useAction, useRequete } from '../hooks/useRequete'

/** Écran formateur (F2) : ouvrir une session, voir le tableau. */
export default function EcranFormateur() {
  const [promotionId, setPromotionId] = useState(null)

  return (
    <>
      <h2>Espace formateur</h2>
      <section>
        <ChoixPromotion valeur={promotionId} onChange={setPromotionId} />
      </section>
      {promotionId && <Sessions key={promotionId} promotionId={promotionId} />}
    </>
  )
}

function Sessions({ promotionId }) {
  const [titre, setTitre] = useState('')
  const ouverture = useAction(api.ouvrirSession)
  const liste = useRequete(`sessions-${promotionId}`, () => api.sessions(promotionId))

  async function ouvrir(evenement) {
    evenement.preventDefault()
    const session = await ouverture.executer(titre, promotionId)
    if (session) {
      setTitre('')
      liste.recharger()
    }
  }

  return (
    <>
      <section>
        <h3>Ouvrir une session</h3>
        <form onSubmit={ouvrir}>
          <label htmlFor="titre">Titre de la séance</label>
          <input id="titre" value={titre} onChange={(e) => setTitre(e.target.value)} placeholder="Séance 3 - Tests" />
          <button className="principal" disabled={ouverture.enCours}>
            {ouverture.enCours ? 'Ouverture…' : 'Ouvrir et obtenir le code'}
          </button>
        </form>
        <MessageErreur erreur={ouverture.erreur} />
        {ouverture.resultat && (
          <div>
            <p className="code-presence">{ouverture.resultat.code}</p>
            <p>Code valable jusqu'à <strong>{heure(ouverture.resultat.expirationAt)}</strong> (15 minutes).</p>
          </div>
        )}
      </section>

      <section>
        <h3>Sessions de la promotion</h3>
        {liste.chargement && <Chargement />}
        <MessageErreur erreur={liste.erreur} />
        {liste.donnees?.length === 0 && <p className="vide">Aucune session pour l'instant.</p>}
        {liste.donnees?.length > 0 && (
          <div className="table-defilante">
            <table>
              <thead>
                <tr><th>Séance</th><th>Code</th><th>Ouverte le</th><th>État</th></tr>
              </thead>
              <tbody>
                {liste.donnees.map((s) => (
                  <tr key={s.id}>
                    <td>{s.titre}</td>
                    <td><code>{s.code}</code></td>
                    <td>{dateHeure(s.ouvertureAt)}</td>
                    <td>{s.cloturee ? <span className="badge">clôturée</span> : <span className="badge attente">ouverte</span>}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>
    </>
  )
}
