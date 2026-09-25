import { useState } from 'react'
import EcranEtudiant from './ecrans/EcranEtudiant'
import EcranFormateur from './ecrans/EcranFormateur'
import EcranRelecteur from './ecrans/EcranRelecteur'

// F2 — Trois écrans : formateur, étudiant, relecteur. Pas d'authentification (Q1).
const ECRANS = [
  { id: 'formateur', libelle: 'Formateur' },
  { id: 'etudiant', libelle: 'Étudiant' },
  { id: 'relecteur', libelle: 'Relecteur' },
]

function Ecran({ id }) {
  if (id === 'formateur') return <EcranFormateur />
  if (id === 'relecteur') return <EcranRelecteur />
  return <EcranEtudiant />
}

export default function App() {
  const [ecran, setEcran] = useState('etudiant')

  return (
    <div className="app">
      <header>
        <h1>PresenceLab</h1>
        <nav>
          {ECRANS.map((e) => (
            <button
              key={e.id}
              className={ecran === e.id ? 'actif' : ''}
              onClick={() => setEcran(e.id)}
            >
              {e.libelle}
            </button>
          ))}
        </nav>
      </header>
      <main>
        <Ecran id={ecran} />
      </main>
    </div>
  )
}
