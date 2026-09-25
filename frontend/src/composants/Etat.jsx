/** Affichage uniforme des états de chargement et d'erreur (F3, ENF6). */
export function Chargement({ texte = 'Chargement…' }) {
  return <p className="chargement" role="status">{texte}</p>
}

export function MessageErreur({ erreur }) {
  if (!erreur) return null
  return (
    <p className="erreur" role="alert">
      {erreur.message} {erreur.code && <code>({erreur.code})</code>}
    </p>
  )
}

export function MessageSucces({ children }) {
  if (!children) return null
  return <p className="succes" role="status">{children}</p>
}
