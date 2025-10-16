Dokument Wymagań Projektowych (PRD): Aplikacja do Zarządzania Zasobami (MVP)

1. Wprowadzenie i Cel Produktu
   Celem aplikacji jest dostarczenie prostego i skutecznego narzędzia do monitorowania codziennych zadań i zasobów
   tworzonych przez pracowników. Produkt ma na celu zwiększenie transparentności procesów, poprawę odpowiedzialności
   zespołowej oraz usprawnienie pracy administracyjnej menedżerów. Wersja MVP skupia się na dostarczeniu kluczowego
   cyklu funkcjonalnego: tworzenia zadań, ich codziennego przesyłania przez pracowników i monitorowania przez
   przełożonych.

2. Role Użytkowników i Uprawnienia
   System definiuje cztery role użytkowników (EmployeeRole) z następującymi uprawnieniami:

ADMIN (3)

Pełen dostęp do wszystkich danych i funkcji w systemie.

Zarządza (dodaje, modyfikuje, deaktywuje) wszystkimi użytkownikami i zespołami.

Może przypisać użytkownikowi każdą z dostępnych ról.

Tworzy i zarządza zadaniami dla wszystkich zespołów.

MANAGER (2)

Dostęp do danych wyłącznie w ramach swojego zespołu (lub zespołów).

Dodaje nowych użytkowników do swojego zespołu.

Modyfikuje komplet danych podległych mu użytkowników, w tym:

Dane osobowe (zgodne ze strukturą Auth0).

Status (np. zmiana z ACTIVE na HOLIDAY).

Rolę (np. zmiana EMPLOYEE na HR).

Tworzy i zarządza zadaniami dla swojego zespołu.

EMPLOYEE (1)

Przesyła przydzielone mu codzienne zadania.

Ma wgląd w historię swoich zgłoszeń.

Widzi swój profil i podstawowe informacje.

HR (4)

Posiada wgląd (tylko do odczytu) do danych wszystkich użytkowników i raportów w celach administracyjnych.

Nie ma uprawnień do tworzenia zadań i zarządzania zespołami.

3. Kluczowe Funkcjonalności (MVP)
   3.1. Zarządzanie Użytkownikami
   Struktura Danych Użytkownika: System operuje na danych zsynchronizowanych z Auth0, obejmujących m.in. name,
   familyName, email oraz user_metadata (w tym roleId i roleName).

Statusy Użytkownika: Każdy użytkownik posiada status (PROBATION, ACTIVE, HOLIDAY, MEDICAL_LEAVE, OFF_WORK, SUSPENDED,
TERMINATED, REGISTERED).

Deaktywacja Zamiast Usuwania: Użytkownik, który miał przypisane jakiekolwiek zadanie, nie jest trwale usuwany. Jego
status jest zmieniany na TERMINATED, a jego historyczne dane pozostają w systemie z oznaczeniem "Nieaktywny".

3.2. Zarządzanie Zadaniami
Tworzenie Zadań: Menedżerowie i Admini mogą tworzyć zadania.

Struktura Zadania: Zadanie składa się z:

ID: Unikalny identyfikator generowany przez system, widoczny dla wszystkich ról.

Tytuł: Pole tekstowe.

Opis: Dłuższe pole tekstowe na instrukcje.

Dynamiczne Pola: Do zadania można dodawać niestandardowe pola:

Krótki tekst

Długi tekst

Liczba

Data (z użyciem datepicker z walidacją uniemożliwiającą wybór daty z przyszłości).

Przypisywanie i Kopiowanie: Zadania są przypisywane do całych zespołów. Istnieje funkcja "Kopiuj istniejące zadanie".

Blokada Edycji: Zadanie jest blokowane przed edycją i usunięciem po otrzymaniu pierwszej odpowiedzi od pracownika.

3.3. Cykl Pracy Pracownika
Dashboard: Po zalogowaniu pracownik widzi pulpit z widżetem "Mój Profil" i listą "Zadania na dziś".

Przesyłanie Zgłoszenia: Wypełnienie formularza i jego wysłanie jest pojedynczą akcją (kliknięcie "Wyślij").

Historia: Pracownik ma dostęp do osobnej zakładki z historią swoich zgłoszeń.

3.4. Monitoring i Raportowanie
Widok Zgłoszeń: Menedżerowie widzą listę zgłoszeń swojego zespołu, z możliwością filtrowania po dacie i pracowniku.

Szczegóły Zgłoszenia: Kliknięcie wiersza na liście otwiera okno modalne z pełnym widokiem wszystkich odpowiedzi danego
zgłoszenia.

Raport Opóźnień: Dostępny jest dedykowany widok/raport pokazujący pracowników, którzy nie przesłali zadań na czas.
Zadania te otrzymują flagę "opóźnione".

Powiadomienia: System wysyła automatyczne powiadomienia e-mail do pracowników, którzy nie wypełnili zadania na godzinę
przed końcem dnia pracy.

4. Założenia Techniczne
   Uwierzytelnianie i autoryzacja są obsługiwane przez zewnętrzną usługę Auth0.

Architektura oparta jest o mikroserwisy oraz oddzielny interfejs graficzny (GUI).

5. Funkcjonalności Poza Zakresem MVP
   Zaawansowane pulpity analityczne i wykresy.

Eksport danych do plików CSV/Excel.

Automatyczne, cykliczne zadania (szablony).

Dodatkowe powiadomienia (np. dla menedżerów).

Możliwość dodawania obrazów/ikon do zadań.

Integracje z systemami zewnętrznymi (np. Jira).