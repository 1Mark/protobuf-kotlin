import com.example.tutorial.protos.AddressBook
import com.example.tutorial.protos.Person
import com.example.tutorial.protos.PersonKt.phoneNumber
import com.example.tutorial.protos.addressBook
import com.example.tutorial.protos.copy
import com.example.tutorial.protos.person
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.io.path.inputStream
import kotlin.io.path.outputStream
import kotlin.system.exitProcess

// This function fills in a Person message based on user input.
fun promptPerson(): Person = person {
    print("Enter person ID: ")
    this.id = readln().toInt()

    print("Enter name: ")
    this.name = readln()

    print("Enter email address (blank for none): ")
    val email = readln()
    if (email.isNotEmpty()) {
        this.email = email
    }

    while (true) {
        print("Enter a phone number (or leave blank to finish): ")
        val number = readln()
        if (number.isEmpty()) break

        print("Is this a mobile, home, or work phone? ")
        val type = when (readln()) {
            "mobile" -> Person.PhoneType.MOBILE
            "home" -> Person.PhoneType.HOME
            "work" -> Person.PhoneType.WORK
            else -> {
                println("Unknown phone type.  Using home.")
                Person.PhoneType.HOME
            }
        }
        this.phones += phoneNumber {
            this.number = number
            this.type = type
        }
    }
}

// Iterates though all people in the AddressBook and prints info about them.
fun print(addressBook: AddressBook) {
    for (person in addressBook.peopleList) {
        println("Person ID: ${person.id}")
        println("  Name: ${person.name}")
        person.email?.let {
            println("  Email address: ${person.email}")
        }
        for (phoneNumber in person.phonesList) {
            val modifier = when (phoneNumber.type) {
                Person.PhoneType.MOBILE -> "Mobile"
                Person.PhoneType.HOME -> "Home"
                Person.PhoneType.WORK -> "Work"
                else -> "Unknown"
            }
            println("  $modifier phone #: ${phoneNumber.number}")
        }
    }
}

// Usage via gradle
// ./gradlew run --args='add_person address_book.txt' --console=plain
// ./gradlew run --args='list_person address_book.txt'
fun main(arguments: Array<String>) {
    require(arguments.size == 2) {
        "Usage: 'add_person PATH_TO_ADDRESS_BOOK' or 'list_person PATH_TO_ADDRESS_BOOK'"
    }
    when (arguments.firstOrNull()) {
        "add_person" -> {
            // Reads the entire address book from a file, adds one person based
            // on user input, then writes it back out to the same file.
            val path = Path(arguments[1])
            val addressBook = readAddressBookFromFile(path)
            writeToAddressBookFile(path, addressBook)
        }

        "list_person" -> {
            // Reads the entire address book from a file, adds one person based
            // on user input, then writes it back out to the same file.
            Path(arguments[1]).inputStream().use {
                print(AddressBook.newBuilder().mergeFrom(it).build())
            }
        }

        else -> {
            println("Usage: 'add_person PATH_TO_ADDRESS_BOOK' or 'list_person PATH_TO_ADDRESS_BOOK'")
            exitProcess(-1)
        }
    }
}

private fun writeToAddressBookFile(path: Path, initialAddressBook: AddressBook) {
    path.outputStream().use {
        initialAddressBook.copy { people += promptPerson() }.writeTo(it)
    }
}

fun readAddressBookFromFile(path: Path): AddressBook {
    if (!path.exists()) {
        println("File not found. Creating new file.")
        return addressBook {}
    } else {
        return path.inputStream().use {
            AddressBook.newBuilder().mergeFrom(it).build()
        }
    }
}
