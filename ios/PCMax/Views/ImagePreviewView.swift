import SwiftUI

/// پیش‌نمایش تمام‌صفحه‌ی عکس بازی — دقیقاً همان تصویر اصلی، بدون کراپ.
struct ImagePreviewView: View {
    @Environment(\.dismiss) private var dismiss
    let url: String

    @State private var appeared = false

    var body: some View {
        ZStack(alignment: .topTrailing) {
            Color.black.opacity(0.92)
                .ignoresSafeArea()
                .onTapGesture { dismiss() }

            RemoteImage(url: url, contentMode: .fit, showBackground: false)
                .padding(20)
                .scaleEffect(appeared ? 1 : 0.92)
                .opacity(appeared ? 1 : 0)
                .allowsHitTesting(false)

            Button {
                dismiss()
            } label: {
                Image(systemName: "xmark")
                    .font(.system(size: 18, weight: .semibold))
                    .foregroundColor(.white)
                    .frame(width: 46, height: 46)
                    .background(Theme.surface)
                    .clipShape(Circle())
                    .overlay(Circle().stroke(Theme.border, lineWidth: 1))
            }
            .padding(16)
        }
        .onAppear {
            withAnimation(.easeOut(duration: 0.26)) { appeared = true }
        }
    }
}
