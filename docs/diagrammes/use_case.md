graph TD
Formateur((Formateur))
Etudiant((Étudiant))
Relecteur((Relecteur))

    Formateur --> UC1[Créer une session / POST /api/sessions]
    Formateur --> UC2[Consulter le tableau récapitulatif / GET /api/tableau]
    
    Etudiant --> UC3[Marquer sa présence / POST /api/presences]
    Etudiant --> UC4[Déposer le lien de l'exercice / POST /api/exercices]
    
    Relecteur --> UC5[Rendre une note et commentaire / POST /api/relectures/{id}]