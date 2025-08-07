# Enhanced Pagination System

## Overview

The HBZCleaner help system now features a comprehensive pagination system with clickable navigation, better page validation, and improved user experience.

## Key Features

### 1. **Clickable Navigation**

- **Previous/Next Buttons**: Click to navigate between pages
- **Hover Tooltips**: Show destination page information
- **Smart Disabling**: Buttons are grayed out when not applicable

### 2. **Improved Page Layout**

- **8 Commands Per Page**: Increased from 6 for better chat space utilization
- **Clean Header**: Modern Adventure API components with proper formatting
- **Footer Tips**: Quick navigation hints for users

### 3. **Enhanced Commands**

```
/hbzlag help          - Shows page 1 (default)
/hbzlag help <page>   - Jump to specific page
/hbzlag h <page>      - Short alias
```

### 4. **Better Error Handling**

- **Page Validation**: Checks if page number exists before displaying
- **Clear Error Messages**: "Page #X does not exist. Valid pages: 1-Y"
- **Number Validation**: Handles invalid input gracefully

## Technical Implementation

### Navigation Components

```java
// Previous page button (clickable when available)
Component prevButton = Component.text("« Previous", NamedTextColor.GREEN)
    .hoverEvent(HoverEvent.showText(Component.text("Go to page " + (currentPage - 1))))
    .clickEvent(ClickEvent.runCommand("/hbzlag help " + (currentPage - 1)));

// Next page button (clickable when available)
Component nextButton = Component.text("Next »", NamedTextColor.GREEN)
    .hoverEvent(HoverEvent.showText(Component.text("Go to page " + (currentPage + 1))))
    .clickEvent(ClickEvent.runCommand("/hbzlag help " + (currentPage + 1)));
```

### Page Management

```java
// Configurable commands per page
private static final int COMMANDS_PER_PAGE = 8;

// Helper methods
public static int getTotalPages()           // Get total number of pages
public static boolean isValidPage(int page) // Validate page number
public static void send(Player p)           // Default to page 1
public static void send(Player p, int page) // Specific page
```

## User Experience Improvements

### Visual Enhancements

1. **Modern Header**: Uses Adventure API components with proper colors and formatting
2. **Color-Coded Navigation**:
   - Green: Clickable navigation buttons
   - Dark Gray: Disabled navigation buttons
   - Aqua: Page numbers
   - Yellow: Command text with hover tooltips

### Interactive Elements

1. **Click Navigation**: Players can click Previous/Next buttons
2. **Page Jumping**: Direct command input for specific pages
3. **Hover Information**: Tooltips show where buttons will take you
4. **Quick Tips**: Footer shows `/hbzlag help <page>` hint for multi-page help

### Error Prevention

1. **Page Bounds Checking**: Prevents accessing non-existent pages
2. **Input Validation**: Handles non-numeric input gracefully
3. **Clear Feedback**: Informative error messages with valid range

## Example Output

```
✦ HBZCleaner Help (Page 1/3) ✦

/hbzlag help(h) <num>     [Hover: Lists all available commands | Permission: hbzcleaner.help | ADMIN ONLY]
/hbzlag status(st)        [Hover: Quick server health overview | Permission: hbzcleaner.status | ADMIN ONLY]
/hbzlag master(m)         [Hover: Comprehensive performance overview | Permission: hbzcleaner.master | ADMIN ONLY]
...

« Previous [1/3] Next »   [Clickable navigation]
Tip: Use /hbzlag help <page> to jump to any page
```

## Benefits

1. **Better Navigation**: Easy to browse through multiple pages of commands
2. **Improved Accessibility**: Click navigation reduces typing
3. **Clear Feedback**: Users always know their current page and total pages
4. **Scalable**: Automatically adjusts as more commands are added
5. **Professional UI**: Modern, clean interface matching current Minecraft standards

## Compatibility

- **Paper 1.20.6+**: Full clickable navigation support
- **Folia**: Compatible with threading model
- **Console**: Falls back to traditional text-based pagination
- **Mobile/Touch**: Click events work with touch interfaces

## Configuration

The pagination system is easily configurable:

- `COMMANDS_PER_PAGE`: Adjust how many commands show per page
- Page validation automatically adjusts to content
- Navigation buttons adapt to available pages
- Fallback support for non-interactive environments
