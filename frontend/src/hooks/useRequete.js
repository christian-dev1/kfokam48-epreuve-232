import { useCallback, useEffect, useRef, useState } from 'react'

/**
 * Charge une donnée depuis l'API et expose les états de chargement et d'erreur (F3).
 * `cle` identifie la requête : quand elle change, on recharge ; `null` suspend l'appel.
 */
export function useRequete(cle, charger) {
  const [etat, setEtat] = useState({ cle: null, donnees: null, erreur: null })
  const [version, setVersion] = useState(0)
  const chargerRef = useRef(charger)

  useEffect(() => {
    chargerRef.current = charger
  })

  useEffect(() => {
    if (cle === null) return undefined
    const cleCourante = `${cle}#${version}`
    let annule = false
    chargerRef.current()
      .then((donnees) => !annule && setEtat({ cle: cleCourante, donnees, erreur: null }))
      .catch((erreur) => !annule && setEtat({ cle: cleCourante, donnees: null, erreur }))
    return () => {
      annule = true
    }
  }, [cle, version])

  const recharger = useCallback(() => setVersion((v) => v + 1), [])
  const aJour = cle !== null && etat.cle === `${cle}#${version}`
  return {
    donnees: aJour ? etat.donnees : null,
    erreur: aJour ? etat.erreur : null,
    chargement: cle !== null && !aJour,
    recharger,
  }
}

/** Exécute une action (POST/PUT) en exposant enCours / erreur / resultat. */
export function useAction(action) {
  const [etat, setEtat] = useState({ enCours: false, erreur: null, resultat: null })

  const executer = useCallback(
    async (...args) => {
      setEtat({ enCours: true, erreur: null, resultat: null })
      try {
        const resultat = await action(...args)
        setEtat({ enCours: false, erreur: null, resultat })
        return resultat
      } catch (erreur) {
        setEtat({ enCours: false, erreur, resultat: null })
        return null
      }
    },
    [action],
  )

  return { ...etat, executer }
}
