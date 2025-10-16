# Dokument wymagań produktu (PRD) - Resource Management Application

## 1. Przegląd produktu

Aplikacja do Zarządzania Zasobami to narzędzie zaprojektowane w celu usprawnienia codziennego raportowania i
monitorowania zadań w organizacji. Umożliwia menedżerom tworzenie i przypisywanie niestandardowych, codziennych zadań do
swoich zespołów, a pracownikom łatwe i szybkie ich przesyłanie. System zapewnia wgląd w czasie rzeczywistym w realizację
zadań, zwiększając przejrzystość operacyjną i odpowiedzialność pracowników.

Głównym celem wersji MVP jest dostarczenie kluczowych funkcjonalności do codziennego raportowania, a nie kompleksowego
zarządzania projektami. Aplikacja jest skierowana do menedżerów, którzy potrzebują efektywnego narzędzia do
monitorowania wydajności zespołu, oraz do pracowników, którzy muszą w prosty sposób raportować swoją codzienną pracę.

## 2. Problem użytkownika

W wielu organizacjach brakuje scentralizowanego i wydajnego systemu do śledzenia codziennych zadań i zasobów, co
prowadzi do kilku kluczowych problemów:

- Problem 1: Brak wglądu w czasie rzeczywistym. Menedżerowie nie mają natychmiastowego dostępu do informacji o tym,
  jakie zadania zostały wykonane danego dnia, co utrudnia ocenę produktywności i szybkie reagowanie na problemy.
- Problem 2: Obciążenie administracyjne. Ręczne zbieranie raportów od pracowników jest czasochłonne, podatne na błędy i
  nieefektywne, co odciąga menedżerów od strategicznych zadań.
- Problem 3: Niska odpowiedzialność pracowników. Brak ustrukturyzowanego procesu raportowania może prowadzić do
  niejasności co do oczekiwań i braku odpowiedzialności za codzienne obowiązki.
- Problem 4: Trudności w zarządzaniu zespołem. Dodawanie nowych pracowników do procesów raportowania oraz zarządzanie
  ich dostępem i rolami bywa skomplikowane i wymaga interwencji działu IT.

Nasza aplikacja rozwiązuje te problemy, automatyzując proces zbierania danych, zapewniając natychmiastowy wgląd w
postępy i upraszczając administrację użytkownikami.

## 3. Wymagania funkcjonalne

### 3.1. Zarządzanie Użytkownikami i Rolami

- Integracja z Auth0: System wykorzystuje istniejącą integrację z Auth0 do uwierzytelniania użytkowników.
- Role użytkowników: System obsługuje cztery role z różnymi uprawnieniami:
    - EMPLOYEE: Może wykonywać i przeglądać własne zadania.
    - MANAGER: Zarządza swoim zespołem, tworzy i przypisuje zadania, monitoruje zgłoszenia.
    - ADMIN: Posiada pełne uprawnienia do zarządzania systemem.
    - HR: Rola zdefiniowana, specyficzne uprawnienia do określenia w przyszłości.
- Statusy użytkowników: Każdy użytkownik ma przypisany status (np. PROBATION, ACTIVE, HOLIDAY, SICK_LEAVE, TERMINATED).
- Zarządzanie cyklem życia użytkownika: Menedżerowie mogą dodawać nowych użytkowników do swojego zespołu, modyfikować
  ich dane (w tym rolę i status) oraz ich deaktywować (status TERMINATED). Deaktywowani użytkownicy pozostają w systemie
  w celu zachowania danych historycznych.

### 3.2. Zarządzanie Zadaniami

- Tworzenie zadań: Menedżerowie i Administratorzy mogą tworzyć nowe zadania.
- Dynamiczna struktura zadań: Zadanie składa się z systemowego ID, tytułu, opisu oraz dynamicznie dodawanych pól (tekst,
  liczba, data).
- Walidacja pól: Pola typu data wykorzystują komponent datepicker i uniemożliwiają wybór daty z przyszłości.
- Kopiowanie zadań: Użytkownicy z uprawnieniami mogą tworzyć nowe zadania, kopiując istniejące, co przyspiesza pracę.
- Przypisywanie zadań: Zadania są przypisywane do całych zespołów, a nie do pojedynczych pracowników.
- Blokada edycji: Zadanie jest automatycznie blokowane przed edycją po otrzymaniu pierwszej odpowiedzi od pracownika,
  aby zapewnić spójność danych.

### 3.3. Wypełnianie i Przesyłanie Zadań

- Dashboard pracownika: Po zalogowaniu pracownik widzi dashboard z listą zadań do wykonania na bieżący dzień.
- Proces przesyłania: Wypełnienie i wysłanie formularza zadania jest pojedynczą transakcją.
- Historia zgłoszeń: Pracownik ma dostęp do historii swoich przesłanych zadań.

### 3.4. Monitoring i Powiadomienia

- Podgląd zgłoszeń: Menedżerowie mają dostęp do listy zgłoszeń swojego zespołu z możliwością filtrowania.
- Szczegóły zgłoszenia: Pełne odpowiedzi na zadanie są dostępne w oknie modalnym po kliknięciu w dany wiersz na liście.
- Raport opóźnień: Dostępny jest dedykowany raport pokazujący zadania, które nie zostały złożone na czas.
- Powiadomienia e-mail: System automatycznie wysyła powiadomienia e-mail do pracowników, którzy nie złożyli przypisanego
  zadania w wyznaczonym terminie.

## 4. Granice produktu

### W zakresie MVP:

- Podstawowe funkcje zarządzania użytkownikami (dodawanie, edycja, deaktywacja) przez Menedżerów w obrębie ich zespołów.
- Tworzenie i przypisywanie zadań z dynamicznymi polami (tekst, liczba, data) do zespołów.
- Funkcja "Kopiuj istniejące zadanie".
- Dashboard dla pracownika z zadaniami na bieżący dzień.
- Składanie odpowiedzi na zadania.
- Widok dla Menedżera z listą odpowiedzi zespołu i raportem opóźnień.
- Automatyczne powiadomienia e-mail o niezłożonych zadaniach.
- Uwierzytelnianie użytkowników za pomocą istniejącej integracji z Auth0.

### Poza zakresem MVP:

- Zaawansowane pulpity analityczne i dashboardy dla zarządu.
- Eksport danych do formatów zewnętrznych (np. CSV, Excel).
- Integracje z zewnętrznymi systemami (np. kalendarze, systemy HR).
- Rozbudowany system szablonów zadań.
- Niestandardowe typy powiadomień (np. push, SMS).
- Przypisywanie zadań do indywidualnych użytkowników (w MVP tylko do zespołów).
- Mechanizmy zatwierdzania zmian wprowadzanych przez menedżerów (np. zmiana roli podwładnego). Kwestia szerokich
  uprawnień menedżerów jest zidentyfikowanym ryzykiem, które zostanie obsłużone procesowo, a nie systemowo w ramach MVP.

## 5. Historyjki użytkowników

### 5.1. Uwierzytelnianie i Autoryzacja

- ID: US-001
- Tytuł: Logowanie użytkownika do systemu
- Opis: Jako użytkownik (Pracownik, Menedżer, Admin, HR), chcę móc bezpiecznie zalogować się do aplikacji przy użyciu
  moich poświadczeń z Auth0, aby uzyskać dostęp do moich zadań i funkcji zgodnych z moją rolą.
- Kryteria akceptacji:
    1. Na stronie logowania znajduje się przycisk "Zaloguj się".
    2. Kliknięcie przycisku przekierowuje użytkownika do interfejsu logowania Auth0.
    3. Po pomyślnym uwierzytelnieniu w Auth0, użytkownik jest przekierowywany z powrotem do aplikacji.
    4. Aplikacja otrzymuje token dostępu użytkownika.
    5. Użytkownik jest zalogowany i widzi interfejs odpowiedni dla swojej roli (np. dashboard pracownika lub panel
       menedżera).
    6. W przypadku nieudanego logowania, użytkownik widzi odpowiedni komunikat o błędzie.

### 5.2. Zarządzanie Zespołem (Perspektywa Menedżera)

- ID: US-002
- Tytuł: Dodawanie nowego pracownika do zespołu
- Opis: Jako Menedżer, chcę móc dodać nowego pracownika do mojego zespołu, podając jego dane, aby mógł on zacząć
  korzystać z aplikacji i raportować zadania.
- Kryteria akceptacji:
    1. W panelu zarządzania zespołem znajduje się opcja "Dodaj użytkownika".
    2. Po jej wybraniu otwiera się formularz z polami: Imię, Nazwisko, Adres e-mail, Rola, Status, Zespół.
    3. Pole "Zespół" jest domyślnie ustawione na zespół menedżera i nie można go zmienić.
    4. Pola Imię, Nazwisko i E-mail są wymagane.
    5. Rola może być wybrana z listy (EMPLOYEE, MANAGER, HR).
    6. Status może być wybrany z listy (PROBATION, ACTIVE, etc.).
    7. Po zapisaniu, nowy użytkownik jest widoczny na liście członków zespołu.
    8. Użytkownik jest tworzony w systemie i powiązany z Auth0.

- ID: US-003
- Tytuł: Modyfikacja danych pracownika
- Opis: Jako Menedżer, chcę móc edytować dane (dane osobowe, status, rolę) członka mojego zespołu, aby utrzymać
  aktualność informacji.
- Opis:
    1. Na liście członków zespołu przy każdym użytkowniku znajduje się opcja "Edytuj".
    2. Po kliknięciu otwiera się formularz z wypełnionymi aktualnymi danymi użytkownika.
    3. Menedżer może modyfikować wszystkie dane, włączając w to rolę (np. z EMPLOYEE na MANAGER) i status (np. z ACTIVE
       na HOLIDAY).
    4. Zmiany są zapisywane i odzwierciedlone na liście użytkowników oraz wpływają na uprawnienia użytkownika.

- ID: US-004
- Tytuł: Deaktywacja pracownika
- Opis: Jako Menedżer, chcę móc deaktywować konto pracownika, który zakończył pracę, aby odebrać mu dostęp do systemu
  przy jednoczesnym zachowaniu jego danych historycznych.
- Kryteria akceptacji:
    1. Edytując dane użytkownika, mogę zmienić jego status na "TERMINATED".
    2. Po zapisaniu zmiany, użytkownik traci możliwość logowania się do systemu.
    3. Deaktywowany użytkownik nie jest usuwany z bazy danych.
    4. Wszystkie historyczne zgłoszenia deaktywowanego użytkownika pozostają widoczne w raportach.

### 5.3. Zarządzanie Zadaniami (Perspektywa Menedżera/Admina)

- ID: US-005
- Tytuł: Tworzenie nowego zadania
- Opis: Jako Menedżer lub Admin, chcę móc stworzyć nowe, niestandardowe zadanie z dynamicznymi polami, aby zebrać od
  pracowników dokładnie te informacje, których potrzebuję.
- Kryteria akceptacji:
    1. W panelu zarządzania zadaniami jest dostępna opcja "Stwórz zadanie".
    2. Otwiera się formularz tworzenia zadania, który zawiera pola: Tytuł, Opis.
    3. Mogę dodawać dynamiczne pola do formularza, wybierając ich typ z listy: Tekst, Liczba, Data.
    4. Dla każdego pola mogę zdefiniować etykietę (np. "Liczba wygenerowanych leadów").
    5. Pola typu Data używają komponentu datepicker, który nie pozwala na wybór przyszłych dat.
    6. Po skonfigurowaniu zadania mogę je zapisać.

- ID: US-006
- Tytuł: Kopiowanie istniejącego zadania
- Opis: Jako Menedżer lub Admin, chcę móc skopiować istniejące zadanie, aby szybko stworzyć nowe, podobne zadanie bez
  potrzeby konfigurowania go od zera.
- Kryteria akceptacji:
    1. Na liście zadań przy każdym zadaniu jest opcja "Kopiuj".
    2. Po jej kliknięciu otwiera się formularz tworzenia nowego zadania, wstępnie wypełniony danymi (tytuł, opis,
       dynamiczne pola) z kopiowanego zadania.
    3. Tytuł skopiowanego zadania ma dopisek " (kopia)".
    4. Wszystkie pola można edytować przed zapisaniem jako nowe zadanie.

- ID: US-007
- Tytuł: Przypisywanie zadania do zespołu
- Opis: Jako Menedżer, chcę móc przypisać stworzone zadanie do mojego zespołu na konkretny dzień.
- Kryteria akceptacji:
    1. Podczas tworzenia lub edycji zadania mogę wybrać zespół, do którego ma być przypisane (Menedżer widzi tylko swój
       zespół, Admin widzi wszystkie).
    2. Mogę określić datę, na którą zadanie jest przypisane.
    3. Po przypisaniu, zadanie pojawi się na dashboardach wszystkich członków wybranego zespołu w określonym dniu.

- ID: US-008
- Tytuł: Blokada edycji zadania po otrzymaniu pierwszej odpowiedzi
- Opis: Jako Menedżer, chcę, aby zadanie było automatycznie blokowane przed edycją po tym, jak pierwszy pracownik
  prześle swoją odpowiedź, w celu zapewnienia spójności danych.
- Kryteria akceptacji:
    1. Gdy zadanie nie ma jeszcze żadnych odpowiedzi, opcja "Edytuj" jest aktywna.
    2. Po przesłaniu przez dowolnego pracownika pierwszej odpowiedzi na to zadanie, opcja "Edytuj" staje się
       nieaktywna (wyszarzona).
    3. Próba edycji zablokowanego zadania (np. przez bezpośredni URL) kończy się niepowodzeniem i komunikatem dla
       użytkownika.

### 5.4. Realizacja Zadań (Perspektywa Pracownika)

- ID: US-009
- Tytuł: Przeglądanie codziennych zadań
- Opis: Jako Pracownik, po zalogowaniu chcę zobaczyć na swoim dashboardzie listę zadań przypisanych do mnie na dzisiaj,
  aby wiedzieć, co mam do zaraportowania.
- Kryteria akceptacji:
    1. Strona główna po zalogowaniu to dashboard pracownika.
    2. Na dashboardzie widoczna jest sekcja "Zadania na dziś".
    3. W tej sekcji znajduje się lista zadań z ich tytułami i statusami (np. "Do wykonania", "Złożone").
    4. Jeśli na dany dzień nie ma przypisanych zadań, wyświetlany jest odpowiedni komunikat.

- ID: US-010
- Tytuł: Wypełnianie i przesyłanie zadania
- Opis: Jako Pracownik, chcę móc otworzyć zadanie, wypełnić wszystkie wymagane pola zgodnie z ich typem (tekst, liczba,
  data) i przesłać je jednym kliknięciem.
- Kryteria akceptacji:
    1. Kliknięcie zadania na dashboardzie otwiera formularz z jego polami.
    2. Mogę wprowadzić dane tekstowe w pola tekstowe, liczbowe w pola liczbowe.
    3. W polach daty mogę wybrać datę z przeszłości lub teraźniejszości za pomocą datepickera.
    4. Po wypełnieniu formularza klikam przycisk "Wyślij".
    5. Po pomyślnym przesłaniu widzę komunikat potwierdzający, a status zadania na dashboardzie zmienia się na "
       Złożone".
    6. Przesłane zadanie nie może być ponownie edytowane.

- ID: US-011
- Tytuł: Przeglądanie historii swoich zgłoszeń
- Opis: Jako Pracownik, chcę mieć dostęp do historii moich przesłanych zadań, aby móc sprawdzić, co raportowałem w
  przeszłości.
- Kryteria akceptacji:
    1. W interfejsie użytkownika jest dostępna sekcja "Historia zgłoszeń".
    2. W tej sekcji widzę listę moich historycznych zgłoszeń posortowaną od najnowszej.
    3. Każdy element listy zawiera tytuł zadania i datę zgłoszenia.
    4. Kliknięcie w element pozwala zobaczyć szczegóły mojego zgłoszenia (moje odpowiedzi).

### 5.5. Monitoring i Raportowanie (Perspektywa Menedżera)

- ID: US-012
- Tytuł: Monitorowanie listy zgłoszeń zespołu
- Opis: Jako Menedżer, chcę widzieć listę wszystkich zgłoszeń od członków mojego zespołu, aby monitorować ich pracę w
  czasie rzeczywistym.
- Kryteria akceptacji:
    1. W panelu menedżera znajduje się widok "Zgłoszenia zespołu".
    2. Widok prezentuje tabelę ze zgłoszeniami, zawierającą kolumny: Tytuł zadania, Pracownik, Data złożenia, Status.
    3. Lista jest domyślnie posortowana od najnowszych zgłoszeń.
    4. Mam możliwość filtrowania listy po pracowniku, dacie i zadaniu.
    5. Kliknięcie w dowolny wiersz tabeli otwiera okno modalne ze szczegółowymi odpowiedziami udzielonymi przez
       pracownika.

- ID: US-013
- Tytuł: Dostęp do raportu opóźnionych zadań
- Opis: Jako Menedżer, chcę mieć dostęp do dedykowanego raportu, który pokazuje, którzy pracownicy nie złożyli swoich
  zadań na czas, abym mógł podjąć odpowiednie działania.
- Kryteria akceptacji:
    1. W panelu menedżera dostępna jest zakładka "Raport opóźnień".
    2. Raport pokazuje listę zadań z przeszłości, które nie zostały wykonane przez przypisanych do nich pracowników z
       mojego zespołu.
    3. Lista zawiera informacje: Tytuł zadania, Imię i nazwisko pracownika, Data, na którą zadanie było przypisane.

### 5.6. Powiadomienia

- ID: US-014
- Tytuł: Otrzymywanie powiadomień e-mail o niezłożonych zadaniach
- Opis: Jako Pracownik, chcę otrzymać przypomnienie e-mail, jeśli nie złożyłem swojego dziennego zadania do końca dnia
  roboczego, abym nie zapomniał o swoich obowiązkach.
- Kryteria akceptacji:
    1. System codziennie po zakończeniu dnia roboczego (np. o godzinie 18:00) sprawdza, czy wszyscy pracownicy złożyli
       przypisane im na ten dzień zadania.
    2. Jeśli pracownik ma niezłożone zadanie, na jego adres e-mail wysyłana jest automatyczna wiadomość.
    3. E-mail zawiera przypomnienie oraz link do aplikacji.
    4. Pracownicy, którzy złożyli zadania lub nie mieli ich przypisanych, nie otrzymują powiadomienia.

Sukces wdrożenia wersji MVP produktu będzie mierzony za pomocą następujących wskaźników, bez definiowania na tym etapie
szczegółowych, liczbowych KPI.

