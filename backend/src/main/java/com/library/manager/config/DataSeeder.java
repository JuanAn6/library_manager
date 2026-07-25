package com.library.manager.config;

import com.library.manager.model.*;
import com.library.manager.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Component
@Profile("dev")
public class DataSeeder implements CommandLineRunner {

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final CategoryRepository categoryRepository;
    private final PublisherRepository publisherRepository;
    private final MemberRepository memberRepository;
    private final LoanRepository loanRepository;

    public DataSeeder(BookRepository bookRepository,
                      AuthorRepository authorRepository,
                      CategoryRepository categoryRepository,
                      PublisherRepository publisherRepository,
                      MemberRepository memberRepository,
                      LoanRepository loanRepository) {
        this.bookRepository = bookRepository;
        this.authorRepository = authorRepository;
        this.categoryRepository = categoryRepository;
        this.publisherRepository = publisherRepository;
        this.memberRepository = memberRepository;
        this.loanRepository = loanRepository;
    }

    @Override
    public void run(String... args) {
        if (bookRepository.count() > 0) {
            System.out.println("Books already present, seeder skipped");
            return;
        }

        // --- Authors ---
        Author marquez = new Author("Gabriel", "García Márquez", "Colombian", LocalDate.of(1927, 3, 6));
        Author orwell = new Author("George", "Orwell", "British", LocalDate.of(1903, 6, 25));
        Author cervantes = new Author("Miguel", "de Cervantes", "Spanish", LocalDate.of(1547, 9, 29));
        Author bradbury = new Author("Ray", "Bradbury", "American", LocalDate.of(1920, 8, 22));
        Author cortazar = new Author("Julio", "Cortázar", "Argentine", LocalDate.of(1914, 8, 26));
        authorRepository.saveAll(List.of(marquez, orwell, cervantes, bradbury, cortazar));

        // --- Categories ---
        Category magicalRealism = new Category("Magical Realism", "Realistic narrative with magical elements");
        Category dystopia = new Category("Dystopia", "Undesirable or frightening future societies");
        Category classic = new Category("Classic", "Canonical works of literature");
        Category sciFi = new Category("Science Fiction", "Speculative fiction based on science and technology");
        categoryRepository.saveAll(List.of(magicalRealism, dystopia, classic, sciFi));

        // --- Publishers ---
        Publisher sudamericana = new Publisher("Editorial Sudamericana", "Argentina", "https://www.penguinlibros.com");
        Publisher secker = new Publisher("Secker & Warburg", "United Kingdom", "https://www.penguin.co.uk");
        Publisher francisco = new Publisher("Juan de la Cuesta", "Spain", null);
        Publisher ballantine = new Publisher("Ballantine Books", "United States", "https://www.penguinrandomhouse.com");
        publisherRepository.saveAll(List.of(sudamericana, secker, francisco, ballantine));

        // --- Books ---
        Book cienAnos = new Book("Cien años de soledad", "9780307474728", LocalDate.of(1967, 5, 30));
        cienAnos.setAuthors(Set.of(marquez));
        cienAnos.setCategory(magicalRealism);
        cienAnos.setPublisher(sudamericana);
        cienAnos.setLanguage("Spanish");
        cienAnos.setPageCount(417);
        cienAnos.setLocation("A-1");
        cienAnos.setTotalCopies(3);
        cienAnos.setAvailableCopies(3);

        Book nineteen = new Book("1984", "9780451524935", LocalDate.of(1949, 6, 8));
        nineteen.setAuthors(Set.of(orwell));
        nineteen.setCategory(dystopia);
        nineteen.setPublisher(secker);
        nineteen.setLanguage("English");
        nineteen.setPageCount(328);
        nineteen.setLocation("B-2");
        nineteen.setTotalCopies(2);
        nineteen.setAvailableCopies(2);

        Book quijote = new Book("El Quijote", "9788424116381", LocalDate.of(1605, 1, 16));
        quijote.setAuthors(Set.of(cervantes));
        quijote.setCategory(classic);
        quijote.setPublisher(francisco);
        quijote.setLanguage("Spanish");
        quijote.setPageCount(863);
        quijote.setLocation("C-3");
        quijote.setTotalCopies(4);
        quijote.setAvailableCopies(4);

        Book fahrenheit = new Book("Fahrenheit 451", "9781451673319", LocalDate.of(1953, 10, 19));
        fahrenheit.setAuthors(Set.of(bradbury));
        fahrenheit.setCategory(sciFi);
        fahrenheit.setPublisher(ballantine);
        fahrenheit.setLanguage("English");
        fahrenheit.setPageCount(194);
        fahrenheit.setLocation("D-4");
        fahrenheit.setTotalCopies(2);
        fahrenheit.setAvailableCopies(2);

        Book rayuela = new Book("Rayuela", "9788437604572", LocalDate.of(1963, 6, 28));
        rayuela.setAuthors(Set.of(cortazar));
        rayuela.setCategory(classic);
        rayuela.setPublisher(sudamericana);
        rayuela.setLanguage("Spanish");
        rayuela.setPageCount(736);
        rayuela.setLocation("C-5");
        rayuela.setTotalCopies(1);
        rayuela.setAvailableCopies(1);

        bookRepository.saveAll(List.of(cienAnos, nineteen, quijote, fahrenheit, rayuela));

        // --- Members ---
        Member ana = new Member("Ana", "López", "ana.lopez@example.com", "600111222", LocalDate.of(2023, 1, 15));
        ana.setAddress("Calle Mayor 1, Madrid");
        Member luis = new Member("Luis", "Martín", "luis.martin@example.com", "600333444", LocalDate.of(2024, 6, 1));
        luis.setAddress("Avenida del Sol 20, Sevilla");
        memberRepository.saveAll(List.of(ana, luis));

        // --- Loans (an active one and a returned one) ---
        Loan activeLoan = new Loan(nineteen, ana, LocalDate.now().minusDays(3), LocalDate.now().plusDays(11));
        activeLoan.setStatus(LoanStatus.ACTIVE);
        // Reflect the borrowed copy on the book
        nineteen.setAvailableCopies(nineteen.getAvailableCopies() - 1);

        Loan returnedLoan = new Loan(fahrenheit, luis, LocalDate.now().minusDays(20), LocalDate.now().minusDays(6));
        returnedLoan.setReturnDate(LocalDate.now().minusDays(8));
        returnedLoan.setStatus(LoanStatus.RETURNED);

        loanRepository.saveAll(List.of(activeLoan, returnedLoan));
        bookRepository.save(nineteen);

        System.out.printf(
                "Seeder finished: %d authors, %d categories, %d publishers, %d books, %d members, %d loans%n",
                authorRepository.count(), categoryRepository.count(), publisherRepository.count(),
                bookRepository.count(), memberRepository.count(), loanRepository.count());
    }
}
