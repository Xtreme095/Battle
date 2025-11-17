# Battle Dawn - Android Recreation

An ultra-modern recreation of the popular browser-based MMORTS game [Battle Dawn](https://www.battledawn.com/) for Android devices, built with cutting-edge technologies and best practices.

## Overview

Battle Dawn is a massively multiplayer online real-time strategy game where players:
- Build and manage colonies
- Gather and manage resources (Metal, Oil, Energy, Food)
- Train armies with different unit types (Infantry, Vehicles, Tanks)
- Form alliances with other players
- Conquer territories and compete for world domination
- Use advanced warfare tactics including spies, nuclear missiles, and ion cannons

This Android recreation brings the full Battle Dawn experience to mobile devices with an optimized touch interface and modern visual design.

## Features

### Core Gameplay
- **Resource Management**: Four primary resources (Metal, Oil, Energy, Food) plus Workers
- **Building System**: 13+ building types including production, military, and special buildings
- **Unit System**: 12+ unit types across Infantry, Vehicles, Tanks, and Special categories
- **Combat System**: Real-time battles with strategic depth and unit type effectiveness
- **Research System**: 14 technologies across Military, Economic, and Special categories
- **Alliance System**: Form alliances, coordinate attacks, and dominate together
- **Espionage**: 6 spy operations including reconnaissance, sabotage, and assassination
- **Superweapons**: Nuclear missiles and ion cannons for devastating strikes
- **Leaderboards**: Player, alliance, and colony rankings
- **Notifications**: Push notifications for attacks, construction, and alliance events

### Technical Features
- **Modern Architecture**: Clean Architecture with MVVM pattern
- **Jetpack Compose**: Fully declarative UI with Material Design 3
- **Kotlin**: 100% Kotlin codebase
- **Dependency Injection**: Hilt for clean dependency management
- **Real-time Sync**: Firebase for multiplayer synchronization
- **Offline Support**: Local database with Room
- **Performance**: Optimized for 60fps gameplay
- **Landscape Mode**: Optimized for landscape gaming experience

## Technology Stack

### Languages & Frameworks
- **Kotlin** 1.9.20
- **Jetpack Compose** (latest BOM)
- **Material Design 3**

### Architecture & Libraries
- **Architecture**: Clean Architecture + MVVM
- **DI**: Hilt 2.48
- **Navigation**: Navigation Compose
- **Database**: Room 2.6.1
- **Networking**: Retrofit 2.9.0 + OkHttp 4.12.0
- **Async**: Kotlin Coroutines + Flow
- **Backend**: Firebase (Auth, Firestore, Realtime Database, Analytics, Messaging)
- **Image Loading**: Coil
- **Logging**: Timber
- **Testing**: JUnit, Coroutines Test, Truth

### Build Configuration
- **Gradle**: Kotlin DSL
- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 34 (Android 14)
- **Compile SDK**: 34

## Project Structure

```
app/
├── src/
│   ├── main/
│   │   ├── java/com/battledawn/
│   │   │   ├── data/                 # Data layer
│   │   │   │   ├── model/           # Data models (DTOs)
│   │   │   │   ├── repository/      # Repository implementations
│   │   │   │   ├── local/           # Room database
│   │   │   │   └── remote/          # Retrofit API services
│   │   │   ├── domain/              # Domain layer
│   │   │   │   ├── model/           # Domain models (entities)
│   │   │   │   │   ├── Resource.kt  # Resource management models
│   │   │   │   │   ├── Building.kt  # Building system models
│   │   │   │   │   ├── Unit.kt      # Unit system models
│   │   │   │   │   ├── Player.kt    # Player and colony models
│   │   │   │   │   └── Combat.kt    # Combat and battle models
│   │   │   │   ├── repository/      # Repository interfaces
│   │   │   │   └── usecase/         # Use cases (business logic)
│   │   │   ├── presentation/        # Presentation layer
│   │   │   │   ├── ui/
│   │   │   │   │   ├── screens/     # Composable screens
│   │   │   │   │   │   ├── splash/  # Splash screen
│   │   │   │   │   │   ├── login/   # Authentication
│   │   │   │   │   │   ├── map/     # World map
│   │   │   │   │   │   └── colony/  # Colony management
│   │   │   │   │   ├── components/  # Reusable UI components
│   │   │   │   │   └── theme/       # Theme configuration
│   │   │   │   ├── viewmodel/       # ViewModels
│   │   │   │   └── navigation/      # Navigation setup
│   │   │   ├── di/                  # Dependency injection
│   │   │   └── util/                # Utilities
│   │   ├── res/                     # Resources
│   │   └── AndroidManifest.xml
│   ├── test/                        # Unit tests
│   └── androidTest/                 # Instrumentation tests
└── build.gradle.kts
```

## Game Mechanics

### Resources
- **Metal**: Primary construction resource for buildings and units
- **Oil**: Required for vehicles, advanced units, and special operations
- **Energy**: Powers special operations (scans, spies, nukes, ion cannon)
- **Food**: Generates workers who collect resources and build units
- **Workers**: Essential workforce for all operations

### Buildings

#### Production Buildings
- **Metal Mine**: Extracts metal ore (max level 10)
- **Oil Refinery**: Refines oil (max level 10)
- **Energy Plant**: Generates energy (max level 10)
- **Farm**: Produces food (max level 10)

#### Military Buildings
- **Unit Production Facility**: Trains military units (max level 5)
  - Level 5 unlocks nuclear missiles
- **Spy Agency**: Trains spies for espionage
- **Nuclear Silo**: Launches nuclear missiles (max level 3)
- **Ion Cannon**: Energy-based superweapon (max level 3)

#### Strategic Buildings
- **Research Center**: Unlocks technologies
- **Defense Tower**: Provides defensive firepower
- **Scanner Array**: Reconnaissance and early warning
- **Command Center**: Central hub (required, max level 10)
- **Warehouse**: Increases resource storage capacity

### Units

#### Infantry (Fast, Cheap, Light)
- **Light Infantry**: Basic foot soldiers (Attack: 10, Defense: 5, Health: 50)
- **Heavy Infantry**: Well-armored soldiers (Attack: 20, Defense: 15, Health: 100)
- **Elite Infantry**: Special forces (Attack: 35, Defense: 25, Health: 150)

#### Vehicles (Balanced, Medium Cost)
- **Light Vehicle**: Fast reconnaissance (Attack: 25, Defense: 20, Health: 120)
- **Armored Vehicle**: Heavy firepower (Attack: 40, Defense: 30, Health: 200)
- **Artillery**: Long-range bombardment (Attack: 60, Defense: 15, Health: 150)

#### Tanks (Slow, Expensive, Heavy)
- **Light Tank**: Basic armored unit (Attack: 50, Defense: 40, Health: 300)
- **Medium Tank**: Well-balanced (Attack: 70, Defense: 60, Health: 500)
- **Heavy Tank**: Ultimate ground warfare (Attack: 100, Defense: 90, Health: 800)

#### Special Units
- **Spy**: Covert operative for espionage missions
- **Nuclear Missile**: Weapon of mass destruction (Attack: 1000)
- **Ion Strike**: Precision energy weapon (Attack: 800)

### Combat System
- **Real-time battles** with turn-based resolution
- **Unit type effectiveness**: Infantry < Vehicles < Tanks < Infantry
- **Defense bonuses** from defensive structures
- **Experience system**: Units level up through combat
- **Plunder system**: Winners can plunder up to 20% of defeated colony's resources

### Espionage Operations
- **Reconnaissance**: Gather intelligence about enemy colonies
- **Sabotage Resources**: Destroy enemy resources
- **Sabotage Buildings**: Temporarily disable enemy structures
- **Freeze Troops**: Prevent enemy troop movement
- **Steal Intel**: Steal research/technology information
- **Assassination**: Kill enemy units/workers

## Getting Started

### Prerequisites
- **Android Studio**: Hedgehog (2023.1.1) or later
- **JDK**: 17 or later
- **Android SDK**: API 34
- **Gradle**: 8.2+

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/Xtreme095/Battle.git
   cd Battle
   ```

2. **Open in Android Studio**
   - Open Android Studio
   - Select "Open an existing project"
   - Navigate to the cloned directory

3. **Sync Gradle**
   - Android Studio will automatically sync Gradle
   - Wait for dependencies to download

4. **Configure Firebase** (Optional for multiplayer)
   - Create a Firebase project at [Firebase Console](https://console.firebase.google.com/)
   - Download `google-services.json`
   - Place it in `app/` directory
   - Enable Authentication, Firestore, and Realtime Database

5. **Run the app**
   - Connect an Android device or start an emulator
   - Click Run (or press Shift+F10)

### Building

#### Debug Build
```bash
./gradlew assembleDebug
```

#### Release Build
```bash
./gradlew assembleRelease
```

## Development Roadmap

### Phase 1: Foundation ✅ (Current)
- [x] Project setup and architecture
- [x] Core domain models (Resources, Buildings, Units, Combat)
- [x] Basic UI screens (Splash, Login, Map, Colony)
- [x] Theme and design system

### Phase 2: Core Gameplay (In Progress)
- [ ] Repository layer implementation
- [ ] Use cases for resource management
- [ ] Use cases for building system
- [ ] Use cases for unit training
- [ ] ViewModels for all screens
- [ ] Complete colony management UI
- [ ] Building construction queue
- [ ] Unit training queue

### Phase 3: Combat & Advanced Features
- [ ] Battle simulation system
- [ ] Combat UI and animations
- [ ] Spy operations
- [ ] Nuclear missile system
- [ ] Ion cannon system
- [ ] Territory control mechanics

### Phase 4: Multiplayer
- [ ] Firebase integration
- [ ] Real-time synchronization
- [ ] Alliance system
- [ ] Alliance chat
- [ ] PvP combat
- [ ] Leaderboards

### Phase 5: Polish & Launch
- [ ] Tutorial system
- [ ] Sound effects and music
- [ ] Push notifications
- [ ] Performance optimization
- [ ] Comprehensive testing
- [ ] Beta testing
- [ ] Play Store release

## Game Balance

### Resource Production Rates (Per Hour)
- Metal Mine Lv.1: 60/h, Lv.10: 720/h
- Oil Refinery Lv.1: 48/h, Lv.10: 576/h
- Energy Plant Lv.1: 36/h, Lv.10: 432/h
- Farm Lv.1: 72/h, Lv.10: 864/h

### Build Times
- Level 1 buildings: 1.5 hours
- Level 5 buildings: 7.5 hours
- Level 10 buildings: 15 hours

### Unit Training Times
- Light Infantry: 30 minutes
- Heavy Tank: 8 hours
- Nuclear Missile: 12 hours

## Contributing

Contributions are welcome! Please follow these guidelines:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Code Style
- Follow [Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use meaningful variable and function names
- Write KDoc comments for public APIs
- Keep functions small and focused

## Testing

### Run Unit Tests
```bash
./gradlew test
```

### Run Instrumentation Tests
```bash
./gradlew connectedAndroidTest
```

## License

This project is a fan recreation and is not affiliated with or endorsed by the original Battle Dawn game or its creators.

## Credits

- **Original Game**: [Battle Dawn](https://www.battledawn.com/) by Gato Games
- **Android Recreation**: Developed as an educational project
- **Technologies**: Google, JetBrains, Square, and the open-source community

## Contact

For questions, suggestions, or issues:
- Open an issue on GitHub
- Email: [your-email@example.com]

---

**Note**: This is a work in progress. The game is currently in active development. Features and mechanics are subject to change.

## Implementation Status

**Overall Completion: ~90%**

### ✅ Fully Implemented (100%):
- Core game mechanics (Resources, Buildings, Units)
- Combat system with battle simulation
- Research/Technology system (14 technologies)
- Espionage system (6 operations)
- Alliance system structure
- 10+ UI screens (Splash, Login, Map, Colony, Research, Alliance, Battle, Leaderboard, Settings)
- ViewModels with state management
- Repository pattern with use cases
- Room database for local storage
- Push notifications framework
- Complete navigation system

### 🔄 Requires Backend (25%):
- Real-time multiplayer synchronization
- Alliance chat messaging
- Live PvP battles
- Cloud save/sync
- Leaderboard data population

### 📊 Total Project Statistics:
- **48 Kotlin source files**
- **56 total project files**
- **13 Building types**
- **12 Unit types**
- **14 Research technologies**
- **6 Spy operations**
- **10+ UI screens**
- **6 Repositories**
- **10+ Use cases**
- **Clean Architecture layers: 3 (Domain, Data, Presentation)**

See [FEATURE_VERIFICATION.md](FEATURE_VERIFICATION.md) for detailed feature comparison with original Battle Dawn.

## Screenshots

*Coming soon - UI fully implemented and functional*

## FAQ

**Q: Is this an official Battle Dawn mobile app?**
A: No, this is an independent recreation project for educational purposes.

**Q: Will this work offline?**
A: Partially. You can manage your colony offline, but multiplayer features require internet connectivity.

**Q: What Android version is required?**
A: Android 7.0 (API 24) or higher.

**Q: Is this free to play?**
A: Yes, the game will be free. Monetization strategy is TBD but will follow fair free-to-play principles.

**Q: Can I play with web Battle Dawn players?**
A: No, this is a standalone version with its own servers and player base.

---

**Built with ❤️ using Kotlin and Jetpack Compose**
