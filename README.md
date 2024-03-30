<h1 align="center">Seekers Server</h1>

<p align="center">
    <a href="https://github.com/seekers-dev/seekers-java/actions/workflows/github-code-scanning/codeql">
        <img src="https://github.com/seekers-dev/seekers-java/actions/workflows/github-code-scanning/codeql/badge.svg" alt="CodeQL">
    </a>
    <a href="https://github.com/seekers-dev/seekers-java/actions/workflows/dependency-review.yml">
        <img src="https://github.com/seekers-dev/seekers-java/actions/workflows/dependency-review.yml/badge.svg" alt="Dependency Review">
    </a>
    <a href="https://github.com/seekers-dev/seekers-java/actions/workflows/maven.yml">
        <img src="https://github.com/seekers-dev/seekers-java/actions/workflows/maven.yml/badge.svg" alt="Java CI with Maven">
    </a>
</p>

In seekers, AIs compete against each other with the aim of scoring as many points as possible. This project is competition-oriented for students.

```mermaid
classDiagram
    Entity: update()
    Entity <|-- Animation
    Entity <|-- Physical
    
    Animation: destroy()
    Animation <|-- GoalAnimation
    Animation <|-- SeekerAnimation
    
    Physical: collision(...)
    Physical <|-- Goal
    Physical <|-- Seeker

    Corresponding: associated()
    Corresponding <|-- Physical
    Corresponding <|-- Player
    Corresponding <|-- Camp
    
    GameMap: getDistance(...)
    GameMap: getDifference(...)
    GameMap: getDirection(...)
    GameMap <|-- TorusMap
```