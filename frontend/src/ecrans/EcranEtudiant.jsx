import { useState } from 'react'
import { ChoixEtudiant } from '../composants/Choix'

/** Écran etudiant (F2) : marquer sa présence, déposer son exercice. */
export default function EcranEtudiant() {
  const [promotionId, setPromotionId] = useState(null)
  const [etudiant, setEtudiant] = useState(null)

  return (
    <>
      <h2>Espace etudiant</h2>
      <ChoixEtudiant promotionId={promotionId} onPromotion={setPromotionId} etudiant={etudiant} onEtudiant={setEtudiant} />
      {etudiant && <p>Bonjour {etudiant.nom}.</p>}
    </>
  )
}
