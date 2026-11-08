package com.example.contacts

import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private val contacts = mutableListOf(
        "Ahmed Ben Ali", "Sarra Trabelsi", "Mohamed Gharbi", "Ines Jaziri",
        "Youssef Mansouri", "Amira Chaabane", "Karim Bouazizi", "Nour Hammami"
    )

    private lateinit var adapter: ArrayAdapter<String>
    private lateinit var etContact: EditText
    private lateinit var btnModifier: Button
    private lateinit var btnAjouter: Button
    private lateinit var lvContacts: ListView

    private var positionSelectionnee = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Exercice 5 : restauration de la liste après rotation de l'écran
        savedInstanceState?.getStringArrayList(CLE_CONTACTS)?.let {
            contacts.clear()
            contacts.addAll(it)
        }

        etContact = findViewById(R.id.etContact)
        btnModifier = findViewById(R.id.btnModifier)
        btnAjouter = findViewById(R.id.btnAjouter)
        lvContacts = findViewById(R.id.lvContacts)

        adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_activated_1,
            contacts
        )
        lvContacts.adapter = adapter

        // Clic : copie le contact dans l'EditText
        lvContacts.setOnItemClickListener { _, _, position, _ ->
            positionSelectionnee = position
            lvContacts.setItemChecked(position, true)
            etContact.setText(contacts[position])
            etContact.setSelection(etContact.text.length)
            etContact.requestFocus()
        }

        // Exercice 2 : appui long = suppression après confirmation
        lvContacts.setOnItemLongClickListener { _, _, position, _ ->
            confirmerSuppression(position)
            true // l'événement est consommé
        }

        btnModifier.setOnClickListener { modifierContact() }
        btnAjouter.setOnClickListener { ajouterContact() }

        etContact.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                modifierContact()
                true
            } else {
                false
            }
        }
    }

    // Sauvegarde la liste avant destruction de l'activité (rotation)
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putStringArrayList(CLE_CONTACTS, ArrayList(contacts))
    }

    private fun modifierContact() {
        if (positionSelectionnee == -1) {
            Toast.makeText(this, R.string.msg_aucune_selection, Toast.LENGTH_SHORT).show()
            return
        }

        val nouveauNom = etContact.text.toString().trim()
        if (nouveauNom.isEmpty()) {
            etContact.error = getString(R.string.msg_nom_vide)
            return
        }

        // Exercice 3 : refuser un nom déjà présent (on ignore la ligne en cours de modification)
        if (existeDeja(nouveauNom, positionSelectionnee)) {
            etContact.error = getString(R.string.msg_doublon)
            return
        }

        contacts[positionSelectionnee] = nouveauNom
        trierEtRafraichir() // Exercice 4
        Toast.makeText(this, "Contact modifié : $nouveauNom", Toast.LENGTH_SHORT).show()
    }

    // Exercice 1 : ajouter un contact
    private fun ajouterContact() {
        val nom = etContact.text.toString().trim()
        if (nom.isEmpty()) {
            etContact.error = getString(R.string.msg_nom_vide)
            return
        }
        if (existeDeja(nom, -1)) {
            etContact.error = getString(R.string.msg_doublon)
            return
        }

        contacts.add(nom)
        trierEtRafraichir()
        Toast.makeText(this, getString(R.string.msg_ajoute, nom), Toast.LENGTH_SHORT).show()
    }

    // Exercice 2 : boîte de dialogue de confirmation
    private fun confirmerSuppression(position: Int) {
        val nom = contacts[position]
        AlertDialog.Builder(this)
            .setTitle(R.string.titre_suppression)
            .setMessage(getString(R.string.msg_confirmer_suppression, nom))
            .setPositiveButton(R.string.oui) { _, _ ->
                contacts.removeAt(position)
                trierEtRafraichir()
            }
            .setNegativeButton(R.string.non, null)
            .show()
    }

    // Exercice 3 : vérifie si un nom existe déjà (sans tenir compte de la casse)
    private fun existeDeja(nom: String, positionAIgnorer: Int): Boolean {
        return contacts.withIndex().any { (index, contact) ->
            index != positionAIgnorer && contact.equals(nom, ignoreCase = true)
        }
    }

    // Exercice 4 : tri alphabétique, puis rafraîchissement et remise à zéro de la sélection
    private fun trierEtRafraichir() {
        contacts.sortBy { it.lowercase() }
        adapter.notifyDataSetChanged()

        // Après un tri, les positions ont changé : on annule la sélection
        positionSelectionnee = -1
        lvContacts.clearChoices()
        etContact.text.clear()
    }

    companion object {
        private const val CLE_CONTACTS = "contacts"
    }
}