import { useState } from 'react'
import { ChoixEtudiant } from '../composants/Choix'

/** Écran relecteur (F2) : l'étudiant voit les exercices qu'il doit relire et rend sa note. */
export default function EcranRelecteur() {
  const [promotionId, setPromotionId] = useState(null)
  const [etudiant, setEtudiant] = useState(null)

  return (
    <>
      <h2>Espace relecteur</h2>
      <ChoixEtudiant promotionId={promotionId} onPromotion={setPromotionId} etudiant={etudiant} onEtudiant={setEtudiant} />
      {etudiant && <p>Bonjour {etudiant.nom}.</p>}
    </>
  )
}
