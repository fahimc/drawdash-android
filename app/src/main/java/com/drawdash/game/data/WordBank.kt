package com.drawdash.game.data

import com.drawdash.game.model.Difficulty
import com.drawdash.game.model.WordEntry

object WordBank {
    private val easy = listOf(
        "Circle:shape", "Square:shape", "Triangle:shape", "Star:shape", "Heart:shape", "Sun:nature",
        "Moon:nature", "Cloud:nature", "Tree:nature", "Flower:nature", "Leaf:nature", "Rain:nature",
        "Snowman:nature", "Mountain:nature", "River:nature", "Cat:animal", "Dog:animal", "Fish:animal",
        "Bird:animal", "Duck:animal", "Pig:animal", "Cow:animal", "Horse:animal", "Mouse:animal",
        "Rabbit:animal", "Bear:animal", "Bee:animal", "Butterfly:animal", "Apple:food", "Banana:food",
        "Pizza:food", "Cake:food", "Cookie:food", "Carrot:food", "Ice Cream:food", "Burger:food",
        "Cup:household", "Chair:household", "Table:household", "Bed:household", "Door:household",
        "Window:household", "Lamp:household", "Clock:household", "Book:object", "Pencil:object",
        "Ball:toys", "Kite:toys", "Balloon:toys", "Boat:transport", "Car:transport", "Bus:transport",
        "Bike:transport", "Train:transport", "Rocket:transport", "House:buildings", "Castle:buildings",
        "Hat:clothing", "Shoe:clothing", "Sock:clothing", "Shirt:clothing", "Dress:clothing",
        "Crown:object", "Key:object", "Flag:object", "Smile:shape", "Arrow:shape", "Ladder:object",
        "Umbrella:object", "Drum:toys", "Teddy Bear:toys", "Crayon:object", "Brush:object",
        "Donut:food", "Pear:food", "Grapes:food", "Orange:food", "Bread:food", "Egg:food",
        "Fork:household", "Spoon:household", "Plate:household", "Bowl:household", "Phone:object",
        "Television:household", "Camera:object", "Glasses:clothing", "Watch:clothing", "Scarf:clothing",
        "Tent:object", "Slide:toys", "Swing:toys", "Dice:toys", "Robot:toys", "Alien:toys",
        "Octopus:animal", "Crab:animal", "Snail:animal", "Frog:animal", "Lion:animal", "Zebra:animal",
        "Palm Tree:nature", "Cactus:nature", "Wave:nature", "Volcano:nature", "Path:nature", "Fence:object",
        "Mailbox:object", "Bridge:buildings", "Tower:buildings", "School:buildings", "Barn:buildings",
        "Helmet:clothing", "Mitten:clothing", "Belt:clothing", "Shorts:clothing", "Skirt:clothing",
        "Candy:food", "Lollipop:food", "Hot Dog:food", "Mushroom:food", "Strawberry:food", "Watermelon:food",
        "Baseball:sports", "Soccer Ball:sports", "Tennis Ball:sports", "Bat:sports", "Goal:sports",
        "Net:sports", "Skate:toys", "Yo-Yo:toys", "Blocks:toys", "Puzzle:toys", "Marble:toys",
        "Rattle:toys", "Box:object", "Bag:object", "Jar:household", "Soap:household", "Toothbrush:household",
        "Broom:household", "Mop:household", "Bucket:household", "Towel:household", "Blanket:household",
        "Sofa:household", "Fan:household", "Radio:household", "Notebook:object", "Envelope:object",
        "Sticker:object", "Button:object", "Comb:object", "Cupboard:household", "Trash Can:household",
        "Peas:food", "Corn:food", "Cheese:food", "Cereal:food", "Milk:food", "Juice:food",
        "Ketchup:food", "Noodles:food", "Fries:food", "Popcorn:food", "Treehouse:buildings",
        "Road:transport", "Crosswalk:transport", "Bench:object", "Sign:object", "Nest:nature"
    )

    private val medium = listOf(
        "Pineapple:food", "Sandwich:food", "Cupcake:food", "Pancakes:food", "Pretzel:food",
        "Broccoli:food", "Pumpkin:food", "Lemon:food", "Peach:food", "Coconut:food", "Avocado:food",
        "Elephant:animal", "Giraffe:animal", "Penguin:animal", "Dolphin:animal", "Shark:animal",
        "Whale:animal", "Turtle:animal", "Chicken:animal", "Monkey:animal", "Koala:animal", "Panda:animal",
        "Airplane:transport", "Helicopter:transport", "Sailboat:transport", "Submarine:transport",
        "Tractor:transport", "Scooter:transport", "Motorcycle:transport", "Truck:transport", "Taxi:transport",
        "Firetruck:transport", "Ambulance:transport", "Violin:object", "Guitar:object", "Trumpet:object",
        "Microphone:object", "Backpack:object", "Suitcase:object", "Treasure Chest:object", "Compass:object",
        "Flashlight:object", "Binoculars:object", "Skyscraper:buildings", "Lighthouse:buildings",
        "Windmill:buildings", "Igloo:buildings", "Cabin:buildings", "Museum:buildings", "Library:buildings",
        "Factory:buildings", "Stadium:buildings", "Castle Gate:buildings", "Raincoat:clothing",
        "Sneaker:clothing", "Boot:clothing", "Necklace:clothing", "Backwards Cap:clothing", "Glove:clothing",
        "Jacket:clothing", "Pajamas:clothing", "Swimsuit:clothing", "Uniform:clothing", "Basketball:sports",
        "Football:sports", "Hockey Stick:sports", "Golf Club:sports", "Skateboard:sports", "Surfboard:sports",
        "Bowling Pin:sports", "Boxing Glove:sports", "Trophy:sports", "Medal:sports", "Seesaw:toys",
        "Toy Car:toys", "Action Figure:toys", "Dollhouse:toys", "Pinwheel:toys", "Train Set:toys",
        "Spinning Top:toys", "Water Gun:toys", "Jump Rope:toys", "Toy Plane:toys", "Clipboard:household",
        "Computer:household", "Keyboard:household", "Mouse Pad:household", "Mirror:household", "Shower:household",
        "Bathtub:household", "Refrigerator:household", "Oven:household", "Toaster:household", "Mixer:household",
        "Sunglasses:clothing", "Earrings:clothing", "Slipper:clothing", "Overalls:clothing", "Tie:clothing",
        "Vase:household", "Candle:household", "Rug:household", "Curtain:household", "Pillow:household",
        "Desk:household", "Bookshelf:household", "Stairs:buildings", "Garage:buildings", "Greenhouse:buildings",
        "Mosque:buildings", "Temple:buildings", "Church:buildings", "Bridge Tower:buildings", "Tunnel:buildings",
        "Dragon:toys", "Wizard Hat:clothing", "Pirate Ship:transport", "Treasure Map:object", "Anchor:object",
        "Canoe:transport", "Kayak:transport", "Roller Skate:sports", "Snowboard:sports", "Climbing Wall:sports",
        "Parrot:animal", "Owl:animal", "Swan:animal", "Peacock:animal", "Camel:animal", "Kangaroo:animal",
        "Ferret:animal", "Fox:animal", "Deer:animal", "Sheep:animal", "Goat:animal", "Llama:animal",
        "Spider:animal", "Ladybug:animal", "Dragonfly:animal", "Seahorse:animal", "Jellyfish:animal",
        "Garden Hose:household", "Picnic Basket:object", "Lunchbox:object", "Snowflake:nature",
        "Pine Cone:nature", "Maple Leaf:nature", "Train Track:transport", "Traffic Cone:object",
        "Bookcase:household", "Doorbell:household", "Sandbox:toys", "Dartboard:sports"
    )

    private val hard = listOf(
        "Accordion:object", "Astronaut:object", "Bicycle Helmet:clothing", "Boomerang:toys", "Campfire:nature",
        "Carnival Tent:buildings", "Chameleon:animal", "Chess Knight:toys", "Clarinet:object",
        "Clothesline:household", "Crescent Moon:nature", "Crocodile:animal", "Double Decker Bus:transport",
        "Eiffel Tower:buildings", "Ferris Wheel:buildings", "Fire Hydrant:object", "Fishing Rod:sports",
        "Flamingo:animal", "Forklift:transport", "Garden Gnome:object", "Grand Piano:object",
        "Hot Air Balloon:transport", "Hummingbird:animal", "Ice Skate:sports", "Jigsaw Piece:toys",
        "Kangaroo Pouch:animal", "Kayak Paddle:sports", "Kettle:household", "Knight Helmet:clothing",
        "Lantern:object", "Lawn Mower:object", "Lobster:animal", "Magnifying Glass:object",
        "Mermaid:toys", "Microscope:object", "Nesting Doll:toys", "Octagon:shape", "Origami Crane:object",
        "Paint Roller:object", "Parachute:transport", "Peacock Feather:animal", "Pocket Watch:object",
        "Postcard:object", "Race Car:transport", "Roller Coaster:buildings", "Saxophone:object",
        "Scarecrow:object", "Scorpion:animal", "Sewing Machine:household", "Ship Wheel:transport",
        "Snow Globe:object", "Space Shuttle:transport", "Stethoscope:object", "Stopwatch:object",
        "Telescope:object", "Toucan:animal", "Traffic Light:object", "Trampoline:sports", "Typewriter:object",
        "Ukulele:object", "Unicorn:toys", "Violin Bow:object", "Waffle Iron:household", "Waterfall:nature",
        "Wheelbarrow:object", "Xylophone:object", "Yoga Mat:sports", "Zipper:clothing", "Zookeeper:object",
        "Acorn:nature", "Anvil:object", "Archway:buildings", "Armadillo:animal", "Badminton Racket:sports",
        "Bagpipes:object", "Ballet Shoe:clothing", "Barcode:object", "Beaker:object", "Bonsai Tree:nature",
        "Bunk Bed:household", "Canoe Oar:sports", "Cassette Tape:object", "Chimney:buildings",
        "Claw Machine:toys", "Compass Rose:object", "Cuckoo Clock:household", "Diving Mask:sports",
        "Domino:toys", "Dreamcatcher:object", "Easel:object", "Fountain:buildings", "Gargoyle:buildings",
        "Gondola:transport", "Gramophone:object", "Harpsichord:object", "Harp:object", "Hourglass:object",
        "Hula Hoop:toys", "Jester Hat:clothing", "Joystick:toys", "Kite String:toys", "Labyrinth:toys",
        "Locomotive:transport", "Luggage Cart:transport", "Maracas:object", "Measuring Tape:object",
        "Merry Go Round:buildings", "Mittens Pair:clothing", "Narwhal:animal", "Oil Lamp:object",
        "Paper Crane:object", "Periscope:object", "Pinball Machine:toys", "Pocket Knife:object",
        "Quill Pen:object", "Rhinoceros:animal", "Satellite:object", "Schooner:transport",
        "Sea Turtle:animal", "Snorkel:sports", "Solar Panel:object", "Steam Train:transport",
        "Tambourine:object", "Teapot:household", "Totem Pole:object", "Viaduct:buildings",
        "Water Tower:buildings", "Weather Vane:object", "Windsock:object", "Zip Line:sports"
    )

    val words: List<WordEntry> = buildList {
        addAll(easy.map { it.toEntry(Difficulty.Easy) })
        addAll(medium.map { it.toEntry(Difficulty.Medium) })
        addAll(hard.map { it.toEntry(Difficulty.Hard) })
    }

    fun choices(used: Set<String>, difficulty: Difficulty, seed: Int): List<WordEntry> {
        val pool = words.filter { it.id !in used && it.difficulty.ordinal <= difficulty.ordinal }
            .ifEmpty { words.filter { it.id !in used } }
            .shuffled(kotlin.random.Random(seed))
        val first = pool.first()
        val second = pool.firstOrNull { it.category != first.category && it.displayName.first() != first.displayName.first() }
            ?: pool.drop(1).first()
        return listOf(first, second)
    }

    private fun String.toEntry(difficulty: Difficulty): WordEntry {
        val parts = split(":")
        val name = parts[0]
        val category = parts.getOrElse(1) { "object" }
        val id = name.lowercase().replace(" ", "_").replace("-", "_")
        val aliases = when (id) {
            "bike" -> listOf("bicycle")
            "television" -> listOf("tv")
            "airplane" -> listOf("plane")
            "soccer_ball" -> listOf("football")
            "hot_air_balloon" -> listOf("balloon")
            else -> emptyList()
        }
        return WordEntry(id, name, category, difficulty, aliases, listOf(name.lowercase()) + aliases)
    }
}
