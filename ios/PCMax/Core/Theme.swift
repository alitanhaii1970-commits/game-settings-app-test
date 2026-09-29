import SwiftUI
import UIKit

extension Color {
    init(hex: UInt32, opacity: Double = 1) {
        self.init(
            .sRGB,
            red: Double((hex >> 16) & 0xFF) / 255,
            green: Double((hex >> 8) & 0xFF) / 255,
            blue: Double(hex & 0xFF) / 255,
            opacity: opacity
        )
    }
}

/// پالت رنگی — دقیقاً همان پالت تیره‌ی نسخه‌ی اندروید
enum Theme {
    static let background = Color(hex: 0x0E0E10)
    static let surface = Color(hex: 0x18181B)
    static let surfaceAlt = Color(hex: 0x212124)
    static let border = Color(hex: 0x2A2A2E)
    static let textPrimary = Color(hex: 0xF5F5F7)
    static let textSecondary = Color(hex: 0x9B9BA3)

    static let red = Color(hex: 0xFF3B41)
    static let redDark = Color(hex: 0xE31E24)
    static let redSoft = Color(hex: 0x3A1518)

    static let green = Color(hex: 0x2ECC71)
    static let greenBG = Color(hex: 0x123321)
    static let yellow = Color(hex: 0xF5C518)
    static let yellowBG = Color(hex: 0x332B0E)
}

// MARK: - فونت‌ها

struct FontChoice: Identifiable {
    let id: String
    let displayName: String
    let regular: String?
    let bold: String?
}

enum AppFonts {
    static let systemId = "system"

    static let options: [FontChoice] = [
        FontChoice(id: systemId, displayName: "پیش‌فرض سیستم / System Default", regular: nil, bold: nil),
        FontChoice(id: "vazirmatn", displayName: "وزیرمتن", regular: "Vazirmatn-Regular", bold: "Vazirmatn-Bold"),
        FontChoice(id: "sahel", displayName: "ساحل", regular: "Sahel", bold: "Sahel-Bold"),
        FontChoice(id: "montserrat", displayName: "Montserrat", regular: "Montserrat-Regular", bold: "Montserrat-Bold"),
        FontChoice(id: "inter", displayName: "Inter", regular: "Inter-Regular", bold: "Inter-Bold")
    ]

    static func font(id: String, size: CGFloat, bold: Bool, style: Font.TextStyle) -> Font {
        let choice = options.first(where: { $0.id == id }) ?? options[0]
        if let name = bold ? choice.bold : choice.regular {
            return Font.custom(name, size: size, relativeTo: style)
        }
        return Font.system(size: size, weight: bold ? .bold : .regular)
    }
}

struct AppFontModifier: ViewModifier {
    @EnvironmentObject var state: AppState
    let size: CGFloat
    let bold: Bool
    let style: Font.TextStyle

    func body(content: Content) -> some View {
        content.font(AppFonts.font(id: state.fontId, size: size, bold: bold, style: style))
    }
}

extension View {
    func appFont(_ size: CGFloat, bold: Bool = false, style: Font.TextStyle = .body) -> some View {
        modifier(AppFontModifier(size: size, bold: bold, style: style))
    }
}

// MARK: - اجزای مشترک ظاهری

struct Backdrop: View {
    var body: some View {
        ZStack {
            Theme.background
            RadialGradient(
                colors: [Theme.red.opacity(0.20), Color.clear],
                center: .top,
                startRadius: 10,
                endRadius: 460
            )
        }
        .ignoresSafeArea()
    }
}

struct PressableStyle: ButtonStyle {
    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .scaleEffect(configuration.isPressed ? 0.97 : 1)
            .opacity(configuration.isPressed ? 0.92 : 1)
            .animation(.spring(response: 0.28, dampingFraction: 0.7), value: configuration.isPressed)
    }
}

struct PrimaryButtonStyle: ButtonStyle {
    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .foregroundColor(.white)
            .frame(maxWidth: .infinity)
            .padding(.vertical, 16)
            .background(
                LinearGradient(colors: [Theme.red, Theme.redDark], startPoint: .top, endPoint: .bottom)
            )
            .clipShape(RoundedRectangle(cornerRadius: 18, style: .continuous))
            .shadow(color: Theme.red.opacity(0.35), radius: 14, x: 0, y: 6)
            .scaleEffect(configuration.isPressed ? 0.96 : 1)
            .animation(.spring(response: 0.28, dampingFraction: 0.7), value: configuration.isPressed)
    }
}

struct RoundIconButton: View {
    let systemName: String
    var rotation: Double = 0
    var dimmed: Bool = false
    var tint: Color = Theme.textPrimary
    let action: () -> Void

    var body: some View {
        Button(action: {
            Haptics.light()
            action()
        }) {
            Image(systemName: systemName)
                .font(.system(size: 17, weight: .semibold))
                .foregroundColor(tint)
                .rotationEffect(.degrees(rotation))
                .frame(width: 44, height: 44)
                .background(Theme.surface)
                .clipShape(Circle())
                .overlay(Circle().stroke(Theme.border, lineWidth: 1))
                .opacity(dimmed ? 0.4 : 1)
        }
        .buttonStyle(PressableStyle())
    }
}

enum Haptics {
    static func light() {
        UIImpactFeedbackGenerator(style: .light).impactOccurred()
    }
    static func success() {
        UINotificationFeedbackGenerator().notificationOccurred(.success)
    }
}

/// کارت گزینه‌ی انتخابی (زبان، قدرت سیستم، فونت)
struct OptionCard<Leading: View>: View {
    @EnvironmentObject var state: AppState
    let title: String
    var subtitle: String? = nil
    let selected: Bool
    let leading: Leading
    let action: () -> Void

    init(title: String, subtitle: String? = nil, selected: Bool,
         action: @escaping () -> Void,
         @ViewBuilder leading: () -> Leading) {
        self.title = title
        self.subtitle = subtitle
        self.selected = selected
        self.action = action
        self.leading = leading()
    }

    var body: some View {
        Button(action: {
            Haptics.light()
            action()
        }) {
            HStack(spacing: 14) {
                leading
                VStack(alignment: .leading, spacing: 3) {
                    Text(title)
                        .appFont(16, bold: true)
                        .foregroundColor(Theme.textPrimary)
                    if let subtitle = subtitle {
                        Text(subtitle)
                            .appFont(12, style: .footnote)
                            .foregroundColor(Theme.textSecondary)
                    }
                }
                Spacer(minLength: 8)
                ZStack {
                    Circle()
                        .stroke(selected ? Theme.red : Theme.border, lineWidth: 2)
                        .frame(width: 24, height: 24)
                    if selected {
                        Circle().fill(Theme.red).frame(width: 14, height: 14)
                            .transition(.scale)
                    }
                }
            }
            .padding(14)
            .background(selected ? Theme.redSoft.opacity(0.55) : Theme.surface)
            .clipShape(RoundedRectangle(cornerRadius: 18, style: .continuous))
            .overlay(
                RoundedRectangle(cornerRadius: 18, style: .continuous)
                    .stroke(selected ? Theme.red : Theme.border, lineWidth: selected ? 1.5 : 1)
            )
            .animation(.easeOut(duration: 0.2), value: selected)
        }
        .buttonStyle(PressableStyle())
    }
}

/// نوار بالای صفحه‌های داخلی (برگشت + عنوان)
struct TopBar: View {
    @EnvironmentObject var state: AppState
    let title: String
    let onBack: () -> Void
    var trailing: AnyView? = nil

    var body: some View {
        HStack(spacing: 12) {
            RoundIconButton(systemName: "chevron.backward", action: onBack)
            Text(title)
                .appFont(20, bold: true, style: .title3)
                .foregroundColor(Theme.textPrimary)
                .lineLimit(1)
            Spacer(minLength: 0)
            if let trailing = trailing { trailing }
        }
        .padding(.horizontal, 16)
        .padding(.top, 8)
        .padding(.bottom, 10)
    }
}

// فعال‌سازی swipe-back با وجود مخفی بودن نوار ناوبری سیستم
extension UINavigationController: UIGestureRecognizerDelegate {
    override open func viewDidLoad() {
        super.viewDidLoad()
        interactivePopGestureRecognizer?.delegate = self
    }

    public func gestureRecognizerShouldBegin(_ gestureRecognizer: UIGestureRecognizer) -> Bool {
        viewControllers.count > 1
    }
}
