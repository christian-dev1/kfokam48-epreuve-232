import { useState } from 'react'
import { ChoixPromotion } from '../composants/Choix'

/** Écran formateur (F2) : ouvrir une session, voir le tableau. */
export default function EcranFormateur() {
  const [promotionId, setPromotionId] = useState(null)

  return (
    <>
      <h2>Espace formateur</h2>
      <section>
        <ChoixPromotion valeur={promotionId} onChange={setPromotionId} />
      </section>
    </>
  )
}
