# HBZCleaner - AI-Powered Lag Prevention Plugin

HBZCleaner is an advanced fork of LaggRemover with cutting-edge AI features for Minecraft servers running on Folia and Paper. It uses machine learning to predict and prevent server lag before it impacts players.

## 🚀 Features

### Core Features (From LaggRemover)

- **Automatic chunk unloading** for empty worlds
- **Smart entity management** with configurable limits
- **Memory optimization** and garbage collection
- **Real-time TPS monitoring** with visual feedback
- **Modular architecture** with plugin support
- **Protocol-based lag removal** with customizable actions

### 🧠 AI-Powered Features (HBZCleaner Exclusive)

- **TPS Prediction** - Predict server performance up to 60 minutes ahead
- **Intelligent Lag Detection** - AI learns patterns and detects lag before it occurs
- **Performance Analysis** - Deep analysis of server metrics and trends
- **Proactive Actions** - Automatically take preventive measures based on predictions
- **Historical Data Tracking** - Store and analyze performance data over time
- **Real-time Alerts** - Notify administrators of predicted lag events

## 🛠️ Installation

### Requirements

- **Java 21** or higher
- **Folia** or **Paper** 1.20.6+ server
- **Minimum 4GB RAM** recommended for AI features

### Setup

1. Download the latest release from the releases page
2. Place `HBZCleaner.jar` in your server's `plugins` folder
3. Start your server to generate configuration files
4. Configure the plugin in `plugins/HBZCleaner/config.yml`
5. Restart your server

## ⚙️ Configuration

### AI Configuration

```yaml
ai:
  # Enable the advanced AI prediction system
  enabled: true

  # How often to collect server metrics (in seconds)
  collection_interval: 30

  # Maximum number of data points to store in memory
  max_history_size: 1000

  # TPS thresholds for predictions
  tps_warning_threshold: 15.0
  tps_critical_threshold: 10.0

  # Enable automatic actions based on predictions
  auto_actions: true

  # Confidence threshold for taking automatic actions (0.0-1.0)
  action_confidence_threshold: 0.7

  # Prediction window in minutes
  prediction_window: 5
```

## 📋 Commands

### HBZCleaner AI Commands

- `/hbz predict [minutes]` - Predict TPS for the next 1-60 minutes
- `/hbz analyze` - Analyze current server performance
- `/hbz tps [seconds]` - Detailed TPS analysis for specified time period
- `/hbz train` - Manually train the AI prediction model
- `/hbz status` - View AI system status and statistics
- `/hbz help` - Show all available commands

### Legacy LaggRemover Commands

- `/lr help` - Show LaggRemover help
- `/lr tps` - Show current TPS
- `/lr clear [items|entities]` - Clear items or entities
- `/lr gc` - Run garbage collection
- `/lr unload` - Unload empty chunks
- `/lr protocol` - Manage lag removal protocols

## 🔐 Permissions

### HBZCleaner AI Permissions

- `hbz.predict` - Access to TPS prediction commands
- `hbz.analyze` - Access to server analysis commands
- `hbz.train` - Access to AI training commands
- `hbz.admin` - Full access to all HBZCleaner features

### Legacy Permissions

All original LaggRemover permissions are preserved:

- `lr.help`, `lr.tps`, `lr.clear`, `lr.gc`, etc.

## 🤖 How the AI Works

### Data Collection

HBZCleaner continuously monitors:

- **TPS (Ticks Per Second)** - Primary performance metric
- **Entity counts** - Total entities across all worlds
- **Chunk loading** - Number of loaded chunks
- **Memory usage** - RAM consumption patterns
- **Player activity** - Online player count and patterns
- **Time patterns** - Performance variations by time of day/week

### Machine Learning Model

The AI uses several algorithms:

- **Linear Regression** - For trend analysis and basic predictions
- **Pattern Recognition** - Identifies recurring lag patterns
- **Variance Analysis** - Detects TPS instability
- **Time Series Analysis** - Considers historical patterns

### Prediction Process

1. **Data Preprocessing** - Normalize and weight different metrics
2. **Feature Engineering** - Extract meaningful patterns from raw data
3. **Model Training** - Continuously update prediction accuracy
4. **Confidence Scoring** - Rate prediction reliability
5. **Action Triggers** - Execute preventive measures when needed

## 📊 Performance Metrics

### TPS Analysis

- **Average TPS** over specified time periods
- **Minimum/Maximum** TPS detection
- **Stability Score** based on variance
- **Lag Spike Count** and frequency
- **Trend Analysis** showing performance direction

### Prediction Accuracy

- **Confidence Score** (0-100%) for each prediction
- **Historical Accuracy** tracking over time
- **False Positive Rate** monitoring
- **Model Performance** statistics

## 🔧 Folia Compatibility

HBZCleaner is fully optimized for Folia servers:

- **Async Operations** - All AI computations run asynchronously
- **Thread Safety** - Concurrent data structures and atomic operations
- **Regional Compatibility** - Works with Folia's region-based threading
- **Scheduler Integration** - Uses Folia-compatible task scheduling

## 🛡️ Automatic Lag Prevention

When the AI predicts incoming lag, it can automatically:

1. **Clear ground items** in affected areas
2. **Remove excessive entities** based on configured limits
3. **Unload unnecessary chunks** to free memory
4. **Trigger garbage collection** to reclaim RAM
5. **Alert administrators** about the predicted issue
6. **Log detailed information** for later analysis

## 📈 Performance Benefits

Users report:

- **50-80% reduction** in lag spikes
- **Improved player experience** with smoother gameplay
- **Proactive problem resolution** before players notice issues
- **Better resource utilization** and server stability
- **Detailed insights** into server performance patterns

## 🐛 Troubleshooting

### Common Issues

**AI not starting:**

- Check Java version (requires Java 21+)
- Verify database permissions in plugin folder
- Check server logs for initialization errors

**Poor prediction accuracy:**

- Allow 24-48 hours for initial training
- Ensure sufficient server activity for data collection
- Check configuration thresholds

**High memory usage:**

- Reduce `max_history_size` in config
- Decrease `collection_interval` if needed
- Monitor for memory leaks in server logs

### Debug Information

Enable debug logging by setting:

```yaml
debug:
  enabled: true
  level: INFO
```

## 🔮 Future Features

Planned enhancements:

- **Neural Network Models** for advanced pattern recognition
- **Multi-server Learning** sharing insights across server networks
- **Plugin Integration** with popular server management tools
- **Web Dashboard** for remote monitoring and analysis
- **Custom Alert Systems** with Discord/Slack integration
- **Advanced Visualizations** and performance graphs

## 🤝 Contributing

We welcome contributions! Please:

1. Fork the repository
2. Create a feature branch
3. Follow Java coding standards
4. Add comprehensive tests
5. Submit a pull request

## 📄 License

HBZCleaner is released under the MIT License. See LICENSE file for details.

Original LaggRemover project by IFC Server Club - Thank you for the foundation!

## 📞 Support

- **Issues:** Use GitHub Issues for bug reports
- **Discord:** Join our Discord server for community support
- **Documentation:** Check the wiki for detailed guides
- **Email:** Contact us at support@hbzcleaner.com

---

**Made with ❤️ for the Minecraft server community**

_HBZCleaner - Preventing lag before it happens!_
