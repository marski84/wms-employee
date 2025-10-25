Jasne, oto podsumowanie rozmowy na temat planowania bazy danych, przygotowane zgodnie z Twoimi wytycznymi.

<conversation_summary>
<decisions>

1. Zaimplementowano trój-tabelowy model dla zadań, obejmujący encje `Task`, `TaskAssignment` i `Submission`.
2. Każdy użytkownik posiada dwa identyfikatory: klucz główny `UUID` w lokalnej bazie danych oraz `auth0_id` do
   integracji z systemem Auth0.
3. Nowi użytkownicy są automatycznie rejestrowani w Auth0, a ich domyślnym statusem jest `PROBATION`.
4. Każdy zespół ma przypisanego jednego menedżera, a próba usunięcia zespołu z pracownikami jest blokowana (
   `ON DELETE RESTRICT`).
5. Zadania i ich przypisania są blokowane przed edycją po pierwszym zgłoszeniu.
6. Wprowadzono walidację uniemożliwiającą ustawienie terminu zadania (`due_date`) w przeszłości.
7. Zdefiniowano cztery role użytkowników z precyzyjnymi uprawnieniami (RLS): `Admin`, `Menedżer`, `HR`, `Pracownik`.
8. Wszystkie daty i znaczniki czasu są przechowywane w bazie w standardzie **UTC** (`TIMESTAMP WITH TIME ZONE`).
9. Wdrożono mechanizm "soft delete" (pole `deleted_at`) dla kluczowych encji.
   </decisions>

<matched_recommendations>

1. **Struktura modułu zadań**: Zgodnie z rekomendacją, przyjęto model oparty na trzech oddzielzielnych encjach (`Task`,
   `TaskAssignment`, `Submission`) w celu precyzyjnego odwzorowania relacji i procesów.
2. **Identyfikatory użytkowników**: Zastosowano zalecenie o używaniu dwóch oddzielnych identyfikatorów dla
   użytkowników (`UUID` i `auth0_id`), co ułatwia integrację z Auth0 przy jednoczesnym zachowaniu niezależności klucza
   głównego.
3. **Zarządzanie zespołami**: Zaimplementowano regułę o przypisaniu dokładnie jednego menedżera do zespołu oraz
   zabezpieczenie integralności bazy danych poprzez `ON DELETE RESTRICT`.
4. **Blokowanie i walidacja**: Przyjęto zalecenia dotyczące blokowania edycji zadań po zgłoszeniu oraz walidacji logiki
   biznesowej, takiej jak data terminu wykonania zadania.
5. **Role i uprawnienia**: Wdrożono szczegółowy model uprawnień oparty na rolach (RLS), zgodnie z rekomendacją, aby
   zapewnić bezpieczeństwo danych na poziomie wierszy.
6. **Standardy techniczne**: Zastosowano się do zaleceń dotyczących przechowywania dat w UTC i stosowania "soft delete"
   w celu zachowania spójności historycznej.
   </matched_recommendations>

<database_planning_summary>
Poniżej znajduje się szczegółowe podsumowanie ustaleń dotyczących planowania bazy danych dla projektu MVP.

**a. Główne wymagania dotyczące schematu bazy danych**

Schemat bazy danych został zaprojektowany z myślą o elastyczności i skalowalności. Główne założenia techniczne obejmują
stosowanie mechanizmu "soft delete" poprzez pole `deleted_at` w kluczowych tabelach, co pozwala na zachowanie danych
historycznych. Wszystkie znaczniki czasu (`TIMESTAMP`) będą przechowywane w standardzie **UTC**, aby zapewnić spójność i
uniknąć problemów ze strefami czasowymi. Integralność danych jest wzmocniona przez ograniczenia na poziomie bazy danych,
takie jak `ON DELETE RESTRICT` dla tabeli zespołów.

**b. Kluczowe encje i ich relacje**

- **Users**: Tabela przechowująca dane o pracownikach. Każdy użytkownik ma unikalny klucz główny `UUID` oraz dodatkową,
  również unikalną, kolumnę `auth0_id` do mapowania z zewnętrznym systemem uwierzytelniania. Domyślny status nowego
  pracownika to `PROBATION`.
- **Teams**: Tabela definiująca zespoły. Każdy zespół ma dokładnie jednego menedżera (`manager_id`), który jest
  powiązany z tabelą `Users`. Relacja ta zapewnia, że zmiana menedżera natychmiast przenosi odpowiednie uprawnienia.
- **Task**: Podstawowa encja przechowująca definicję zadania (np. `title`, `description`). `title` jest polem
  wymaganym (`NOT NULL`).
- **TaskAssignment**: Tabela łącząca zadania (`Task`) z zespołami (`Team`). Reprezentuje fakt przypisania zadania do
  konkretnego zespołu i zawiera termin wykonania (`due_date`). Relacja jeden-do-wielu z `Task` i `Team`.
- **Submission**: Encja reprezentująca zgłoszenie wykonania zadania przez konkretnego pracownika (`User`). Zawiera
  odniesienie do przypisania zadania (`TaskAssignment`) oraz datę zgłoszenia (`submission_date`).

**c. Ważne kwestie dotyczące bezpieczeństwa i skalowalności**

Bezpieczeństwo danych jest realizowane głównie poprzez polityki **Row-Level Security (RLS)**, które ściśle kontrolują
dostęp do danych w zależności od roli użytkownika:

- **Admin**: Pełny dostęp do wszystkich danych w systemie (CRUD).
- **Menedżer**: Może zarządzać (CRUD) tylko członkami, zadaniami i przypisaniami w ramach swojego zespołu. Nie ma
  uprawnień do nadawania roli `MANAGER`.
- **HR**: Dostęp do odczytu danych wszystkich użytkowników oraz listy zespołów.
- **Pracownik**: Dostęp tylko do własnych danych oraz zadań przypisanych do zespołu, do którego należy.

Integracja z Auth0 poprzez dedykowane pole `auth0_id` centralizuje zarządzanie tożsamością i uwierzytelnianiem, co
zwiększa bezpieczeństwo. Zastosowanie "soft delete" i blokowanie edycji zadań po pierwszym zgłoszeniu zapewniają
spójność i audytowalność danych.

</database_planning_summary>

<unresolved_issues>
Na podstawie dostarczonych "Ostatecznych Założeń Projektowych" wydaje się, że wszystkie kluczowe kwestie związane z
planowaniem bazy danych dla etapu MVP zostały omówione i rozstrzygnięte. Brak jest jawnie wskazanych nierozwiązanych
problemów.
</unresolved_issues>
</conversation_summary>